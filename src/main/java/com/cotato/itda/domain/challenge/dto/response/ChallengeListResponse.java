package com.cotato.itda.domain.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ChallengeListResponse(

        @Schema(description = "챌린지 목록")
        List<ChallengeItem> challenges,

        @Schema(
                description = "현재 응답된 목록 중 가장 마지막 챌린지 ID, 다음 데이터 요청 시 이 값을 요청 파라미터로 포함해야 합니다.",
                example = "10")
        Long lastId,

        @Schema(
                description = "다음 챌린지 존재 여부, 현재 반환된 리스트 이후 불러올 데이터가 더 남아있는지 나타냅니다.\n" +
                        "true인 경우, lastId 값을 파라미터에 포함해 다음 데이터를 요청해야 합니다.",
                example = "true"
        )
        boolean hasNext

) {
    @Builder
    public record ChallengeItem(

            @Schema(description = "챌린지를 업로드한 멤버 정보 (ID, 닉네임)")
            MemberInfo memberInfo,

            @Schema(description = "챌린지 ID", example = "1")
            Long challengeId,

            @Schema(description = "챌린지 사진 전체 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diary/uuid_example.jpg")
            String imageUrl,

            @Schema(description = "업로드 시간", example = "2026-01-10T10:00:00")
            LocalDateTime createdAt,

            @Schema(description = "읽음 여부", example = "true")
            boolean isViewed

    ) {
    }
    @Builder
    public record MemberInfo(
            @Schema(description = "멤버 ID")
            Long memberId,

            @Schema(description = "멤버 닉네임 (친구 별명, 별명이 null인 경우 친구 본명")
            String nickname
    ) {
    }
}
