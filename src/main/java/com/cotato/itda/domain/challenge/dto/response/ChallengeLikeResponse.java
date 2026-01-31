package com.cotato.itda.domain.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChallengeLikeResponse(

        @Schema(description = "좋아요 개수",  example = "1")
        int likeCount
) {
}
