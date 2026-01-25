package com.cotato.itda.domain.profile.controller;

import com.cotato.itda.domain.profile.dto.request.ProfileCreateRequest;
import com.cotato.itda.domain.profile.dto.request.ProfileUpdateRequest;
import com.cotato.itda.domain.profile.dto.response.ProfileResponse;
import com.cotato.itda.domain.profile.service.ProfileService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "프로필 API")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "온보딩-프로필 이미지 등록",
            description = """
                    S3에 업로드된 이미지 URL을 받아 사용자의 프로필 이미지를 저장합니다.
                    - 이미지 URL이 null인 경우, 기본 프로필 이미지로 등록됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    @PostMapping
    public ApiResponse<ProfileResponse> createProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @RequestBody ProfileCreateRequest request) {

        Long memberId = jwtPrincipal.memberId();
        ProfileResponse response = profileService.createProfile(memberId, request);

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "프로필 이미지 수정",
            description = """
                    기존 프로필 이미지를 새로운 프로필 이미지로 수정합니다.
                    - 이미지 URL이 null인 경우, 기본 프로필 이미지가 적용됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    @PutMapping
    public ApiResponse<ProfileResponse> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @RequestBody ProfileUpdateRequest request) {

        Long memberId = jwtPrincipal.memberId();
        ProfileResponse response = profileService.updateProfile(memberId, request);

        return ApiResponse.success(response);
    }

    @Operation(summary = "프로필 조회", description = "프로필 이미지, 이름, 전화번호, 생년월일을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
    })
    @GetMapping
    public ApiResponse<ProfileResponse> getProfile(@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal) {

        Long memberId = jwtPrincipal.memberId();
        ProfileResponse response = profileService.getProfile(memberId);

        return ApiResponse.success(response);
    }

}
