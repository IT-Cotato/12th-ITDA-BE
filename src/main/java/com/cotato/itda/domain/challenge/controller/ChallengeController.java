package com.cotato.itda.domain.challenge.controller;

import com.cotato.itda.domain.challenge.dto.request.ChallengeCreateRequest;
import com.cotato.itda.domain.challenge.dto.response.*;
import com.cotato.itda.domain.challenge.service.command.ChallengeCommandService;
import com.cotato.itda.domain.challenge.service.query.ChallengeQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
@Tag(name = "Challenge API", description = "챌린지 및 미션 API")
public class ChallengeController {

    private final ChallengeCommandService challengeCommandService;
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

    @Operation(summary = "챌린지 등록 API", description = "미션 ID, 사진 URL을 전달받아 챌린지를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 미션 ID (오늘 미션 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 미션 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 해당 미션에 대한 챌린지 존재 (미션 참여 완료)")
    })
    @SecurityRequirement(name = "AccessToken")
    @PostMapping
    public ApiResponse<ChallengeResponse> createChallenge(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Valid @RequestBody ChallengeCreateRequest request
    ) {
        Long memberId = jwtPrincipal.memberId();
        ChallengeResponse response = challengeCommandService.createChallenge(memberId, request);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "나의 오늘 미션 참여 여부 및 나의 챌린지 조회 API",
            description = """
                    나의 오늘 미션 참여 여부와 내가 업로드한 챌린지 정보를 조회합니다. (하루기록 탭 상단)
                    - 친구의 챌린지 목록은 친구 챌린지 목록 조회 API(GET /api/challenges)에서 따로 조회합니다.
                    - 오늘 미션에 참여한 경우, 챌린지 정보도 함께 반환하며 미참여 시 null로 반환됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 미션"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/me")
    public ApiResponse<MyChallengeResponse> getMyChallengeStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal
    ) {
        Long memberId = jwtPrincipal.memberId();
        MyChallengeResponse response = challengeQueryService.getMyChallenge(memberId);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "챌린지 상세 조회 API",
            description = "특정 챌린지를 상세 조회합니다. 작성자가 본인이거나, 친구 관계인 경우에만 조회가 가능합니다." )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음 (친구 관계 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("{challengeId}")
    public ApiResponse<ChallengeDetailResponse> getChallengeDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "조회할 챌린지 ID", required = true) @PathVariable Long challengeId
    ) {
        Long memberId = jwtPrincipal.memberId();
        ChallengeDetailResponse response = challengeQueryService.getChallengeDetail(memberId, challengeId);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "친구 챌린지 목록 조회 API",
            description = """
                     친구의 오늘 챌린지 전체 목록을 무한스크롤로 조회합니다.
                    - 챌린지 목록은 최신순으로 반환됩니다.
                    - 챌린지 정보, 멤버 정보, 읽음 여부, 페이징 정보(lastId, hasNext)가 반환됩니다.
                    - 첫 조회: lastId는 null로 요청합니다.
                    - 추가 조회: hasNext가 true인 경우, 응답받은 lastId를 요청 파라미터로 포함해 다음 데이터를 요청합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 파라미터"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping
    public ApiResponse<ChallengeListResponse> getChallengeList(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "직전 조회 결과의 마지막 챌린지 ID (다음 페이지 커서, 첫 조회 시 null)")
            @RequestParam(required = false) Long lastId,

            @Parameter(description = "조회할 챌린지 개수 (기본값 10개)")
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        Long memberId = jwtPrincipal.memberId();
        ChallengeListResponse response = challengeQueryService.getChallengeList(memberId, lastId, size);
        return ApiResponse.success(response);
    }

}
