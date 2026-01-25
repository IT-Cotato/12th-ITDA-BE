package com.cotato.itda.domain.diary.controller;

import com.cotato.itda.domain.diary.dto.request.DiaryCommentRequest;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentListResponse;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentResponse;
import com.cotato.itda.domain.diary.service.command.DiaryCommentCommandService;
import com.cotato.itda.domain.diary.service.query.DiaryCommentQueryService;
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
    private final DiaryCommentQueryService diaryCommentQueryService;

    @Operation(
            summary = "공유일기 댓글 등록",
            description = """
                    pathVariable로 전달받은 특정 일기에 댓글을 등록합니다. 
                    - 등록 후 댓글 정보와 작성자 정보가 반환됩니다.
                    - 본인 또는 친구의 일기에만 댓글을 등록할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "일기 접근 권한 없음 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @PostMapping("/{diaryId}/comments")
    public ApiResponse<DiaryCommentResponse> createDiaryComment(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "일기 ID", required = true) @PathVariable("diaryId") Long diaryId,
            @Valid @RequestBody DiaryCommentRequest request
            ) {
        Long memberId = jwtPrincipal.memberId();

        DiaryCommentResponse response = diaryCommentCommandService.createComment(memberId, diaryId, request);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "공유일기 댓글 삭제",
            description = """
                    pathVariable로 전달받은 특정 댓글을 삭제합니다. 
                    - 작성자 본인만 삭제 가능합니다. 
                    - DB에서 완전히 삭제되지 않고, 삭제 상태로 변경되어 조회되지 않습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "일기 또는 댓글 권한 없음 (작성자만 삭제 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID 또는 댓글 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{diaryId}/comments/{commentId}")
    public ApiResponse<Void> deleteDiaryComment(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "일기 ID", required = true) @PathVariable("diaryId") Long diaryId,
            @Parameter(description = "삭제할 댓글 ID", required = true) @PathVariable("commentId") Long commentId
    ) {
        Long memberId = jwtPrincipal.memberId();

        diaryCommentCommandService.softDeleteComment(memberId, diaryId, commentId);
        return ApiResponse.success(null);
    }

    @Operation(
            summary = "공유일기 댓글 목록 조회",
            description = """
                        특정 일기의 댓글 목록을 무한스크롤로 조회합니다.
                        - 댓글은 작성순(과거순)으로 반환됩니다.
                        - 댓글 정보, 작성자 정보, 페이징 정보(lastId, hasNext)가 반환됩니다.
                        - 첫 조회: lastId는 null로 요청합니다.
                        - 추가 조회: hasNext가 true인 경우, 응답받은 lastId를 요청 파라미터로 포함해 다음 데이터를 요청합니다.
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "일기 접근 권한 없음 "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/{diaryId}/comments")
    public ApiResponse<DiaryCommentListResponse> getDiaryCommentList(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "일기 ID", required = true) @PathVariable("diaryId") Long diaryId,
            @Parameter(description = "직전 조회 결과의 마지막 댓글 ID (다음 페이지 커서, 첫 조회 시 null)") @RequestParam(required = false) Long lastId,
            @Parameter(description = "조회할 댓글 개수") @RequestParam(required = false, defaultValue = "10") int size
    ) {
        Long memberId = jwtPrincipal.memberId();

        DiaryCommentListResponse response = diaryCommentQueryService.getCommentList(diaryId, memberId, lastId, size);
        return ApiResponse.success(response);
    }
}
