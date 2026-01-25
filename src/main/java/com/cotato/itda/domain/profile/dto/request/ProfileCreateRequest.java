package com.cotato.itda.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileCreateRequest(
        @Schema(
                description = "S3 업로드 후 반환된 프로필 이미지의 URL",
                example = "https://...amazonaws.com/profile/uuid_filename.png"
        )
        String profileImageUrl
) {
}
