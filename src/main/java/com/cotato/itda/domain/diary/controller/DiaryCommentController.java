package com.cotato.itda.domain.diary.controller;

import com.cotato.itda.domain.diary.dto.request.DiaryCommentRequest;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentResponse;
import com.cotato.itda.domain.diary.service.command.DiaryCommentCommandService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
@Tag(name = "Diary", description = "공유일기 API")
public class DiaryCommentController {

    private final DiaryCommentCommandService diaryCommentCommandService;

    @Operation(
            summary = "공유일기 댓글 등록",
            description = "특정 일기에 댓글을 등록합니다. 등록 후 댓글 정보와 작성자 정보가 반환됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "일기 접근 권한 없음 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @PostMapping("/{diaryId}/comments")
    public ApiResponse<DiaryCommentResponse> createDiaryComment(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "일기 ID", required = true)
            @PathVariable("diaryId") Long diaryId,

            @Valid @RequestBody DiaryCommentRequest request
            ) {
        Long memberId = jwtPrincipal.memberId();
        DiaryCommentResponse response = diaryCommentCommandService.createComment(memberId, diaryId, request);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "공유일기 댓글 삭제",
            description = "특정 댓글을 삭제합니다. 작성자 본인만 삭제 가능합니다. DB에서 완전히 삭제되지 않고, 삭제 상태로 변경되어 조회되지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "일기 또는 댓글 권한 없음 (작성자만 삭제 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID 또는 댓글 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{diaryId}/comments/{commentId}")
    public ApiResponse<Void> deleteDiaryComment(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "일기 ID", required = true)
            @PathVariable("diaryId") Long diaryId,

            @Parameter(description = "삭제할 댓글 ID", required = true)
            @PathVariable("commentId") Long commentId
    ) {
        Long memberId = jwtPrincipal.memberId();
        diaryCommentCommandService.softDeleteComment(memberId, diaryId, commentId);
        return ApiResponse.success(null);
    }
}
