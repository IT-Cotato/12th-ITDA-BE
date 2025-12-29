package com.cotato.itda.domain.image.dto.request;

import com.cotato.itda.global.common.constant.S3Folder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUrlRequest(
        @Schema(description = "S3 버킷 내 저장될 prefix 경로", example = "profile")
        @NotNull(message = "폴더 명은 필수입니다.")
        S3Folder folder,

        @Schema(description = "파일 원본 이름 (확장자 포함)", example = "profileImage.png")
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName
) {
}
