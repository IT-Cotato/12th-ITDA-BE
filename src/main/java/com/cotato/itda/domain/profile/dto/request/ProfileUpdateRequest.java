package com.cotato.itda.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "프로필 이름")
        @NotBlank
        @Size(max = 20)
        String profileName,

        @Schema(description = "생년월일", example = "19950101")
        @Pattern(regexp = "^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$",
                message = "생년월일은 YYYYMMDD 형식의 8자리 숫자여야 합니다.")
        String birthDate
) {
}
