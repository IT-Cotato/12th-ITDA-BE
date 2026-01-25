package com.cotato.itda.domain.profile.service;

import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.profile.dto.request.ProfileCreateRequest;
import com.cotato.itda.domain.profile.dto.request.ProfileUpdateRequest;
import com.cotato.itda.domain.profile.dto.response.ProfileResponse;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final MemberRepository memberRepository;

    @Value("${spring.cloud.aws.s3.default-image}")
    private String defaultProfileImageUrl;

    @Transactional
    public ProfileResponse createProfile(Long memberId, ProfileCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        String profileImageUrl = (request.profileImageUrl() == null || request.profileImageUrl().isBlank())
                ? defaultProfileImageUrl : request.profileImageUrl();
        member.updateProfile(profileImageUrl);

        return ProfileResponse.from(member);
    }

    @Transactional
    public ProfileResponse updateProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        String profileImageUrl = (request.profileImageUrl() == null || request.profileImageUrl().isBlank())
                ? defaultProfileImageUrl : request.profileImageUrl();
        member.updateProfile(profileImageUrl);

        return ProfileResponse.from(member);
    }

    public ProfileResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        return ProfileResponse.from(member);
    }

}
