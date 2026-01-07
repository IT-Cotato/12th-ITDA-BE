package com.cotato.itda.domain.profile.service;

import com.cotato.itda.domain.image.service.S3Service;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.profile.dto.request.ProfileCreateRequest;
import com.cotato.itda.domain.profile.dto.request.ProfileUpdateRequest;
import com.cotato.itda.domain.profile.dto.response.ProfileCreateResponse;
import com.cotato.itda.domain.profile.dto.response.ProfileResponse;
import com.cotato.itda.global.error.constant.ProfileErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    @Value("${spring.cloud.aws.s3.default-image}")
    private String defaultProfileImageUrl;

    /**
     * 온보딩에서의 프로필 이미지 및 프로필 이름 등록
     */
    public ProfileCreateResponse createProfile(Long memberId, ProfileCreateRequest request) {
        Member member = findMemberById(memberId);

        String profileImageUrl = getOrDefaultImageUrl(request.profileImageUrl());
        String profileName = getOrDefaultProfileName(request.profileName(), member.getName());

        member.updateProfile(profileImageUrl, profileName);

        return new ProfileCreateResponse(member.getProfileImageUrl(), member.getProfileName());
    }

    /**
     * 프로필 수정
     */
    public ProfileResponse updateProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = findMemberById(memberId);

        String profileImageUrl = handleProfileImageUpdate(member.getProfileImageUrl(), request.profileImageUrl());
        String profileName = getOrDefaultProfileName(request.profileName(), member.getName());

        member.updateProfile(profileImageUrl, profileName);

        return ProfileResponse.from(member);
    }

    /**
     * 프로필 조회
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long memberId) {
        Member member = findMemberById(memberId);

        return ProfileResponse.from(member);
    }

    /**
     * 프로필 이미지 삭제
     */
    public ProfileResponse deleteProfileImage(Long memberId) {
        Member member = findMemberById(memberId);
        String currentProfileImageUrl = member.getProfileImageUrl();

        // 기본 이미지거나 null인 경우 삭제 불가
        if (currentProfileImageUrl.equals(defaultProfileImageUrl) || currentProfileImageUrl == null) {
            throw new BusinessException(ProfileErrorCode.PROFILE_IMAGE_NOT_FOUND, Map.of("profileImageUrl", currentProfileImageUrl));
        }

        // S3에서 기존 프로필 이미지 파일 삭제
        s3Service.deleteImage(currentProfileImageUrl);

        // 기본 프로필 이미지로 업데이트
        member.updateToDefaultProfileImage(defaultProfileImageUrl);
        return ProfileResponse.from(member);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(
                        UserErrorCode.USER_NOT_FOUND,
                        Map.of("memberId", memberId)
                ));
    }

    private String getOrDefaultImageUrl(String profileImageUrl) {
        // 프로필 이미지 없는 경우 기본 이미지 사용
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return defaultProfileImageUrl;
        }
        s3Service.validateImageExists(profileImageUrl);
        return profileImageUrl;
    }

    private String getOrDefaultProfileName(String profileName, String memberName) {
        // 프로필 이름 없는 경우 회원 이름으로 설정
        if (profileName == null || profileName.isBlank()) {
            return memberName;
        }
        return profileName;
    }

    /**
     * 프로필 이미지 변경 여부를 확인하여 검증 및 기존 이미지 삭제
     */
    private String handleProfileImageUpdate(String oldUrl, String newUrl) {
        if (newUrl == null || newUrl.equals(oldUrl)) {
            return oldUrl;
        }
        // 기본 이미지가 아닌 새 이미지로 바뀐 경우 객체 검증
        if (newUrl != null && !newUrl.equals(defaultProfileImageUrl)) {
            s3Service.validateImageExists(newUrl);
        }
        // 기존 이미지가 기본 이미지가 아닌 경우 S3에서 삭제
        if (oldUrl != null && !oldUrl.equals(defaultProfileImageUrl)) {
            s3Service.deleteImage(oldUrl);
        }
        return newUrl;
    }

}
