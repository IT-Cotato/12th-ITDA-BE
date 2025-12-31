package com.cotato.itda.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ProfileCreateRequest(
        @Schema(
                description = "S3 업로드 후 반환된 프로필 이미지의 URL",
                example = "https://...amazonaws.com/profile/uuid_filename.png"
        )
        String profileImageUrl,

        @Schema(description = "프로필 이름", example = "영자")
        @Size(max = 20, message = "프로필 이름은 20자 이하여야 합니다.")
        String profileName
) {
}
