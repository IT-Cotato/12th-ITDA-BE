package com.cotato.itda.domain.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileCreateResponse(
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "프로필 이름")
        String profileName
) {
}
