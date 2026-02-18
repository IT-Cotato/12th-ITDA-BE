package com.cotato.itda.domain.friendship.dto.res;

import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.friendship.enums.ChatGoal;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.enums.SpeechStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class FriendshipResDTO {

    @Builder
    public record CreateDTO(
            @Schema(description = "친구 관계 ID", example = "1")
            Long friendshipId,
            @Schema(description = "친구 정보")
            FriendInfoDTO friendInfo,
            @Schema(description = "친구 관계 상태", example = "PENDING")
            FriendshipStatus status,
            @Schema(description = "생성 일시", example = "2025-12-30T00:22:05.36169")
            LocalDateTime createdAt
    ) {
    }

    @Builder
    public record FriendInfoDTO(
            @Schema(description = "친구 회원 ID", example = "2")
            Long id,
            @Schema(description = "친구 이름", example = "홍길동")
            String name,
            @Schema(description = "친구 프로필 이미지 URL", example = "https://example.com/profile.jpg")
            String profileImageUrl
    ) {
    }

    @Builder
    public record UpdateDTO(
            @Schema(description = "친구 관계 ID", example = "1")
            Long friendshipId,
            @Schema(description = "친구 관계 상태", example = "ACTIVE")
            FriendshipStatus status,
            @Schema(description = "수정 일시", example = "2025-12-30T00:22:05.36169")
            LocalDateTime updatedAt
    ) {
    }

    @Builder
    public record FriendshipItemDTO(
            @Schema(description = "친구 관계 ID", example = "1")
            Long friendshipId,
            @Schema(description = "친구 ID", example = "1")
            Long friendId,
            @Schema(description = "표시 이름 (별명이 있으면 별명, 없으면 본명)", example = "길동이")
            String showName,
            @Schema(description = "친구 프로필 이미지 URL", example = "https://example.com/profile.jpg")
            String profileImageUrl,
            @Schema(description = "친구 관계 상태", example = "ACTIVE")
            FriendshipStatus status,
            @Schema(description = "마지막 대화 일시", example = "2025-12-30T00:22:05.36169")
            LocalDateTime lastInteractedAt
    ) {}

    @Builder
    public record FriendshipListDTO(
            @Schema(description = "친구 관계 개수", example = "5")
            Integer count,
            @Schema(description = "친구 관계 목록")
            List<FriendshipItemDTO> friendshipList
    ) {}

    @Builder
    public record FriendshipSettingsDTO(
            @Schema(description = "친구 관계 ID", example = "1")
            Long friendshipId,
            @Schema(description = "친구 별명", example = "길동이")
            String nickname,
            @Schema(description = "대화 말투", example = "존댓말")
            SpeechStyle speechStyle,
            @Schema(description = "대화 목표", example = "주 1일")
            ChatGoal chatGoal,
            @Schema(description = "대화 주제 코드 리스트", example = "[\"HEALTH\", \"WEATHER\"]")
            List<String> topicCodes
    ) {}

    @Builder
    public record SearchByInviteCodeDTO(
            @Schema(description = "회원 ID", example = "1")
            Long memberId,
            @Schema(description = "회원 이름", example = "홍길동")
            String memberName,
            @Schema(description = "회원 프로필 이미지 URL", example = "https://example.com/profile.jpg")
            String profileImageUrl
    ) {}
}
