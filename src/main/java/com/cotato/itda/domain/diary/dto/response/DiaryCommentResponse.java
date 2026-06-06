package com.cotato.itda.domain.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DiaryCommentResponse(

        @Schema(description = "댓글 ID", example = "2")
        Long commentId,

        @Schema(description = "부모 댓글 ID", example = "1")
        Long parentId,

        @Schema(description = "작성자 정보")
        WriterInfo writerInfo,

        @Schema(description = "댓글 내용", example = "재밌었겠다")
        String content,

        @Schema(description = "작성 시간", example = "2026-01-10T10:00:00")
        LocalDateTime createdAt
) {
}
