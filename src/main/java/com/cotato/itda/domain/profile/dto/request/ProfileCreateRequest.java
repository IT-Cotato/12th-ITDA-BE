package com.cotato.itda.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ProfileCreateRequest(
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "프로필 이름")
        @Size(max = 20)
        String profileName
) {
}
