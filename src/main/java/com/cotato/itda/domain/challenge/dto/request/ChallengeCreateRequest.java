package com.cotato.itda.domain.challenge.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChallengeCreateRequest(
        @Schema(description = "오늘 미션 ID", example = "1")
        @NotNull
        Long missionId,

        @Schema(description = "챌린지 전체 사진 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/challenge/uuid_example.jpg")
        @NotBlank(message = "챌린지 사진 URL은 필수입니다.")
        String imageUrl
){
}
