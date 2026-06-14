package com.cotato.itda.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.cotato.itda.domain.notification.enums.NotificationSection;
import com.cotato.itda.domain.notification.enums.NotificationTargetType;
import com.cotato.itda.domain.notification.enums.NotificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NotificationListResponse(
	@Schema(description = "알림 목록")
	List<NotificationItem> notifications,

	@Schema(description = "현재 응답된 목록 중 가장 마지막 알림 ID", example = "10")
	Long lastId,

	@Schema(description = "다음 알림 존재 여부", example = "true")
	boolean hasNext
) {

	@Builder
	public record NotificationItem(
		@Schema(description = "알림 ID", example = "1")
		Long notificationId,

		@Schema(description = "알림 섹션", example = "GARDEN")
		NotificationSection section,

		@Schema(description = "알림 타입", example = "PLANT_INVITE")
		NotificationType type,

		@Schema(description = "알림 본문", example = "이속희님이 초대장을 보냈어요.")
		String content,

		@Schema(description = "프로필 또는 대상 이미지 URL", example = "https://example.com/profile.jpg")
		String imageUrl,

		@Schema(description = "이동 대상 타입", example = "SHARED_PLANT_INVITE")
		NotificationTargetType targetType,

		@Schema(description = "이동 대상 ID", example = "10")
		Long targetId,

		@Schema(description = "읽음 여부", example = "false")
		boolean isRead,

		@Schema(description = "알림 생성 시각", example = "2026-06-14T12:00:00")
		LocalDateTime createdAt
	) {
	}
}
