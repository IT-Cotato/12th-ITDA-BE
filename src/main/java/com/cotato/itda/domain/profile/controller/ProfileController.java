package com.cotato.itda.domain.profile.controller;

import com.cotato.itda.domain.profile.dto.request.ProfileCreateRequest;
import com.cotato.itda.domain.profile.dto.request.ProfileUpdateRequest;
import com.cotato.itda.domain.profile.dto.response.ProfileCreateResponse;
import com.cotato.itda.domain.profile.dto.response.ProfileResponse;
import com.cotato.itda.domain.profile.service.ProfileService;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "프로필 API")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "온보딩-프로필 등록", description = "온보딩에서 프로필 이미지와 프로필 이름을 등록합니다.")
    @PostMapping("/{memberId}")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileCreateResponse> createProfile(
            @PathVariable(name = "memberId") Long memberId,
            @Valid @RequestBody ProfileCreateRequest request) {

        ProfileCreateResponse response = profileService.createProfile(memberId, request);

        return ApiResponse.success(response);
    }

    @Operation(summary = "프로필 수정", description = "프로필을 수정합니다.")
    @PutMapping("/{memberId}")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileResponse> updateProfile(
            @PathVariable(name = "memberId") Long memberId,
            @Valid @RequestBody ProfileUpdateRequest request) {

        ProfileResponse response = profileService.updateProfile(memberId, request);

        return ApiResponse.success(response);
    }

    @Operation(summary = "프로필 조회", description = "프로필을 조회합니다.")
    @GetMapping("/{memberId}")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileResponse> getProfile(
            @PathVariable(name = "memberId") Long memberId) {

        ProfileResponse response = profileService.getProfile(memberId);

        return ApiResponse.success(response);
    }

    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제합니다. ")
    @DeleteMapping("/{memberId}/profile-image")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileResponse> deleteProfileImage(@PathVariable(name = "memberId") Long memberId) {
        ProfileResponse response = profileService.deleteProfileImage(memberId);

        return ApiResponse.success(response);
    }

}
