package com.cotato.itda.domain.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MyChallengeResponse(

        @Schema(description = "나의 오늘 미션 참여 여부", example = "true")
        boolean isCompleted,

        @Schema(description = "챌린지 ID (미참여 시 null)")
        Long challengeId,

        @Schema(description = "챌린지 사진 URL (미참여 시 null)", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/challenge/uuid_example.jpg")
        String imageUrl,

        @Schema(description = "챌린지 생성 시간", example = "2026-01-10T10:00:00")
        LocalDateTime createdAt

) {
}
