package com.cotato.itda.domain.profile.controller;

import com.cotato.itda.domain.profile.dto.request.ProfileCreateRequest;
import com.cotato.itda.domain.profile.dto.request.ProfileUpdateRequest;
import com.cotato.itda.domain.profile.dto.response.ProfileCreateResponse;
import com.cotato.itda.domain.profile.dto.response.ProfileResponse;
import com.cotato.itda.domain.profile.service.ProfileService;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력값 또는 S3에 존재하지 않는 파일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    @PostMapping("/{memberId}")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileCreateResponse> createProfile(
            @PathVariable(name = "memberId") Long memberId,
            @Valid @RequestBody ProfileCreateRequest request) {

        ProfileCreateResponse response = profileService.createProfile(memberId, request);

        return ApiResponse.success(response);
    }

    @Operation(summary = "프로필 수정", description = "프로필 이미지와 프로필 이름을 수정합니다. 프로필 이미지 수정 후 기존 이미지는 S3에서 삭제됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력값 또는 S3에 존재하지 않는 파일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 서버 통신 오류(기존 이미지 삭제 실패)")
    })
    @PutMapping("/{memberId}")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileResponse> updateProfile(
            @PathVariable(name = "memberId") Long memberId,
            @Valid @RequestBody ProfileUpdateRequest request) {

        ProfileResponse response = profileService.updateProfile(memberId, request);

        return ApiResponse.success(response);
    }

    @Operation(summary = "프로필 조회", description = "프로필 이미지, 프로필 이름, 이름, 전화번호, 생년월일을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
    })
    @GetMapping("/{memberId}")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileResponse> getProfile(
            @PathVariable(name = "memberId") Long memberId) {

        ProfileResponse response = profileService.getProfile(memberId);

        return ApiResponse.success(response);
    }

    @Operation(summary = "프로필 이미지 삭제", description = "현재 설정된 프로필 이미지를 S3에서 삭제하고 프로필 이미지를 기본 이미지로 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "삭제할 프로필 이미지가 존재하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 서버 통신 오류"),
    })
    @DeleteMapping("/{memberId}/profile-image")
    // TODO: 인증 로직 통합 후 토큰 정보를 사용하도록 @PathVariable memberId 제거 및 수정
    public ApiResponse<ProfileResponse> deleteProfileImage(@PathVariable(name = "memberId") Long memberId) {
        ProfileResponse response = profileService.deleteProfileImage(memberId);

        return ApiResponse.success(response);
    }

}
