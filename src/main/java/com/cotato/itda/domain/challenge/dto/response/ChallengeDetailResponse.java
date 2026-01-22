package com.cotato.itda.domain.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChallengeDetailResponse(

        @Schema(description = "멤버 정보")
        MemberInfo memberInfo,

        @Schema(description = "챌린지 ID")
        Long challengeId,

        @Schema(description = "챌린지 전체 사진 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/challenge/uuid_example.jpg")
        String imageUrl,

        @Schema(description = "챌린지 생성 시간", example = "2026-01-10T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "좋아요 여부")
        boolean isLiked,

        @Schema(description = "좋아요 개수")
        int likeCount,

        @Schema(description = "댓글 개수")
        int commentCount

) {
    @Builder
    public record MemberInfo(
            @Schema(description = "멤버 ID")
            Long memberId,

            @Schema(description = "멤버 닉네임")
            String nickname
    ) {
    }
}
