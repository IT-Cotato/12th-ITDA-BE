package com.cotato.itda.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlResponse(
        @Schema(description = "S3 업로드를 위한 presigned-url")
        String presignedUrl,

        @Schema(description = "업로드 후 이미지 URL")
        String imageUrl,

        @Schema(description = "업로드 시 Header에 담아야 할 Content-Type")
        String contentType
) {
}
