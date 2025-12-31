package com.cotato.itda.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlResponse(
        @Schema(description = "S3 업로드를 위한 presigned-url", example = "https://...")
        String presignedUrl,

        @Schema(description = "업로드 후 해당 이미지에 접근하기 위한 URL", example = "https://...amazonaws.com/profile/uuid_filename.png")
        String imageUrl,

        @Schema(description = "PUT으로 파일 업로드 시 Header에 담아야 할 Content-Type", example = "image/png")
        String contentType
) {
}
