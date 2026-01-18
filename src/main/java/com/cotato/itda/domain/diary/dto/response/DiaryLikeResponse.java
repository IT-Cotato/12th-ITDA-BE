package com.cotato.itda.domain.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record DiaryLikeResponse(

        @Schema(description = "좋아요 개수", example = "5")
        int likeCount
) {
}
