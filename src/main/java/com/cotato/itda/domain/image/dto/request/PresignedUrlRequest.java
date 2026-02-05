package com.cotato.itda.domain.image.dto.request;

import com.cotato.itda.domain.image.enums.S3Folder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUrlRequest(
        @Schema(description = "S3 버킷 내 저장될 prefix 경로", example = "PROFILE")
        @NotNull(message = "폴더명은 필수입니다.")
        S3Folder folder,

        @Schema(description = "파일 원본 이름 (확장자 포함)", example = "filename.png")
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName
) {
}
