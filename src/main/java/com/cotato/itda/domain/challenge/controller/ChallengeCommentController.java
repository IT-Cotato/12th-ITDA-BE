package com.cotato.itda.domain.challenge.controller;

import com.cotato.itda.domain.challenge.dto.request.ChallengeCommentRequest;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentListResponse;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentResponse;
import com.cotato.itda.domain.challenge.service.command.ChallengeCommentCommandService;
import com.cotato.itda.domain.challenge.service.query.ChallengeCommentQueryService;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
@Tag(name = "Challenge API", description = "챌린지 및 미션 API")
public class ChallengeCommentController {

    private final ChallengeCommentCommandService challengeCommentCommandService;
    private final ChallengeCommentQueryService challengeCommentQueryService;

    @Operation(
            summary = "챌린지 댓글/대댓글 등록 API",
            description = """
                    pathVariable로 전달받은 특정 챌린지에 댓글을 등록합니다. 
                    - 등록 후 댓글 정보와 작성자 정보가 반환됩니다.
                    - 본인 또는 친구의 챌린지에만 댓글을 등록할 수 있습니다.
                    - 대댓글 작성 시, Request Body에 부모 댓글 ID(`parentId`)를 포함해야 합니다.
                    - 대댓글에 다시 대댓글을 달 경우 400 에러가 발생합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "챌린지 접근 권한 없음 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @PostMapping("/{challengeId}/comments")
    public ApiResponse<ChallengeCommentResponse> createChallengeComment(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "챌린지 ID", required = true)
            @PathVariable("challengeId") Long challengeId,

            @Valid @RequestBody ChallengeCommentRequest request
    ) {
        Long memberId = jwtPrincipal.memberId();

        ChallengeCommentResponse response = challengeCommentCommandService.createComment(memberId, challengeId, request);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "챌린지 댓글/대댓글 삭제 API",
            description = """
                    특정 댓글을 삭제합니다. 
                    - 작성자 본인만 삭제 가능합니다. 
                    - DB에서 완전히 삭제되지 않고, 삭제 상태로 변경되어 조회되지 않습니다.
                    - 부모 댓글을 삭제한 경우, "삭제된 댓글입니다."로 마스킹 처리되어 반환됩니다.
                    - 자식 댓글이 남아 있는 경우, 그대로 반환됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "챌린지 또는 댓글 권한 없음 (작성자만 삭제 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 ID 또는 댓글 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{challengeId}/comments/{commentId}")
    public ApiResponse<Void> deleteChallengeComment(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "챌린지 ID", required = true)
            @PathVariable("challengeId") Long challengeId,

            @Parameter(description = "삭제할 댓글 ID", required = true)
            @PathVariable("commentId") Long commentId
    ) {
        Long memberId = jwtPrincipal.memberId();

        challengeCommentCommandService.softDeleteComment(memberId, challengeId, commentId);
        return ApiResponse.success(null);
    }

    @Operation(
            summary = "챌린지 댓글 목록 조회 API",
            description = """
                        특정 챌린지의 댓글 목록을 무한스크롤로 조회합니다.
                        - 댓글은 작성순(과거순)으로 반환됩니다.
                        - 댓글 정보, 작성자 정보, 페이징 정보(lastId, hasNext)가 반환됩니다.
                        - 첫 조회: lastId는 null로 요청합니다.
                        - 추가 조회: 응답의 hasNext가 true인 경우, 응답받은 lastId를 요청 파라미터로 포함해 다음 데이터를 요청합니다.
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "챌린지 접근 권한 없음 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/{challengeId}/comments")
    public ApiResponse<ChallengeCommentListResponse> getChallengeCommentList(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "챌린지 ID", required = true)
            @PathVariable("challengeId") Long challengeId,

            @Parameter(description = "직전 조회 응답의 lastId 값 (첫 조회 요청 시 null)")
            @RequestParam(required = false) Long lastId,

            @Parameter(description = "조회할 댓글 개수 (기본값 10개)")
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        Long memberId = jwtPrincipal.memberId();

        ChallengeCommentListResponse response = challengeCommentQueryService.getCommentList(memberId, challengeId, lastId, size);
        return ApiResponse.success(response);
    }
}
