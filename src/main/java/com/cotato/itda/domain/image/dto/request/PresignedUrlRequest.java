package com.cotato.itda.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
        @Schema(description = "S3 버킷 내 저장될 prefix 경로, (프로필의 경우 profile)", example = "profile")
        @NotBlank(message = "폴더명은 필수입니다.")
        String folder,

        @Schema(description = "파일 원본 이름 (확장자 포함)", example = "myProfileImage.png")
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName
) {
}
