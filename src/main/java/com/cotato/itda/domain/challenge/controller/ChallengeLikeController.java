package com.cotato.itda.domain.challenge.controller;

import com.cotato.itda.domain.challenge.dto.response.ChallengeLikeResponse;
import com.cotato.itda.domain.challenge.service.command.ChallengeLikeCommandService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
@Tag(name = "Challenge API", description = "챌린지 및 미션 API")
public class ChallengeLikeController {

    private final ChallengeLikeCommandService challengeLikeCommandService;

    @Operation(summary = "특정 챌린지 좋아요 API", description = "특정 챌린지에 좋아요를 등록합니다. 이미 좋아요 누른 경우 409 에러가 반환됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "좋아요 권한 없음 (친구 관계 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 좋아요 누른 챌린지"),
    })
    @SecurityRequirement(name = "AccessToken")
    @PostMapping("/{challengeId}/likes")
    public ApiResponse<ChallengeLikeResponse> addLike(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "좋아요할 챌린지 ID", required = true) @PathVariable Long challengeId
    ) {
        Long memberId = jwtPrincipal.memberId();
        ChallengeLikeResponse response = challengeLikeCommandService.addLike(memberId, challengeId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "특정 챌린지 좋아요 취소 API", description = "특정 챌린지에 대한 좋아요를 취소합니다. 취소할 좋아요가 없는 경우 404 에러가 반환됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 ID 또는 취소할 좋아요 없음")
    })
    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{challengeId}/likes")
    public ApiResponse<ChallengeLikeResponse> deleteLike(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "좋아요 취소할 챌린지 ID", required = true) @PathVariable Long challengeId
    ) {
        Long memberId = jwtPrincipal.memberId();
        ChallengeLikeResponse response = challengeLikeCommandService.deleteLike(memberId, challengeId);
        return ApiResponse.success(response);
    }
}
