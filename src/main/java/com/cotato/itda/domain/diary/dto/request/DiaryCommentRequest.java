package com.cotato.itda.domain.diary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiaryCommentRequest(

        @Schema(description = "댓글 내용", example = "재밌었겠다")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(min = 1, max = 5000, message = "댓글 내용은 1자 이상 5000자 이하여야 합니다.")
        String content,

        @Schema(description = "부모 댓글 ID", example = "1")
        Long parentId
) {
}
