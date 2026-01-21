package com.cotato.itda.domain.challenge.controller;

import com.cotato.itda.domain.challenge.dto.response.ChallengeDashboardResponse;
import com.cotato.itda.domain.challenge.service.query.ChallengeQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
@Tag(name = "Challenge API", description = "챌린지 및 미션 조회 API")
public class ChallengeController {

    private final ChallengeQueryService challengeQueryService;

    @Operation(
            summary = "오늘 미션 조회 및 주간 참여 현황 조회 API ",
            description = """
                오늘의 미션 정보를 조회하고, 나의 주간 미션 참여 현황을 조회합니다.
                - 미션 카테고리는 FOOD, PLANT, COLOR, MOMENT, TV 중 하나로 반환됩니다.
                - 오늘 미션 참여 완료 시, 나의 챌린지 정보를 반환하며 미참여 시 null을 반환합니다.
                - 이번주 날짜별 참여 상태(월~일)는 챌린지 여부에 따라 DONE(참여완료), MISSED(미참여), WAITING(오늘이며 미참여), FUTURE(미래)로 반환됩니다.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "미션이 존재하지 않음")
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/dashboard")
    public ApiResponse<ChallengeDashboardResponse> getChallengeDashboard(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal
    ) {
        Long memberId = jwtPrincipal.memberId();
        ChallengeDashboardResponse response = challengeQueryService.getChallengeDashboard(memberId);
        return ApiResponse.success(response);
    }

}
