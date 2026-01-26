package com.cotato.itda.domain.challenge.controller;

import com.cotato.itda.domain.challenge.dto.request.ChallengeCommentRequest;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentResponse;
import com.cotato.itda.domain.challenge.service.command.ChallengeCommentCommandService;
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

    @Operation(
            summary = "챌린지 댓글 등록 API",
            description = """
                    pathVariable로 전달받은 특정 챌린지 댓글을 등록합니다. 
                    - 등록 후 댓글 정보와 작성자 정보가 반환됩니다.
                    - 본인 또는 친구의 챌린지에만 댓글을 등록할 수 있습니다.
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
            summary = "챌린지 댓글 삭제 API",
            description = """
                    pathVariable로 전달받은 특정 댓글을 삭제합니다. 
                    - 작성자 본인만 삭제 가능합니다. 
                    - DB에서 완전히 삭제되지 않고, 삭제 상태로 변경되어 조회되지 않습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "챌린지 또는 댓글 권한 없음 (작성자만 삭제 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 ID 또는 댓글 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{challengeId}/comments/{commentId}")
    public ApiResponse<Void> deleteDiaryComment(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "챌린지 ID", required = true)
            @PathVariable("challengeId") Long diaryId,

            @Parameter(description = "삭제할 댓글 ID", required = true)
            @PathVariable("commentId") Long commentId
    ) {
        Long memberId = jwtPrincipal.memberId();

        challengeCommentCommandService.softDeleteComment(memberId, diaryId, commentId);
        return ApiResponse.success(null);
    }
}
