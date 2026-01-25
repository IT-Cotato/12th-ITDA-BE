package com.cotato.itda.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Schema(
                description = "S3 업로드 후 반환된 프로필 이미지의 URL",
                example = "https://...amazonaws.com/profile/uuid_filename.png"
        )
        String profileImageUrl
) {
}
