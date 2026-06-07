package com.cotato.itda.domain.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DiaryCommentListResponse(

        @Schema(description = "조회된 댓글 목록")
        List<CommentItem> comments,

        @Schema(
                description = "현재 응답된 목록 중 가장 마지막 댓글 ID, 다음 데이터 요청 시 이 값을 요청 파라미터로 포함해야 합니다.",
                example = "10")
        Long lastId,

        @Schema(
                description = "다음 댓글 존재 여부, 현재 반환된 리스트 이후 불러올 데이터가 더 남아있는지 나타냅니다.<br>" +
                        "true인 경우, lastId 값을 파라미터에 포함해 다음 데이터를 요청해야 합니다.",
                example = "true"
        )
        boolean hasNext

) {
    @Builder
    public record CommentItem(

            @Schema(description = "댓글 ID", example = "1")
            Long commentId,

            @Schema(description = "댓글 작성자 정보")
            WriterInfo writerInfo,

            @Schema(description = "댓글 내용", example = "재밌었겠다")
            String content,

            @Schema(description = "작성 시간", example = "2026-01-10T10:00:00")
            LocalDateTime createdAt,

            @Schema(description = "대댓글 목록, 없을 경우 빈 배열이 반환됩니다.")
            List<CommentItem> childComments,

            @Schema(description = "댓글 삭제 여부", example = "false")
            boolean isDeleted
    ) {}
}
