package com.cotato.itda.domain.notification.converter;

import java.util.List;

import com.cotato.itda.domain.notification.dto.NotificationListResponse;
import com.cotato.itda.domain.notification.entity.Notification;

public class NotificationConverter {

	public static NotificationListResponse.NotificationItem toListItem(Notification notification) {
		return NotificationListResponse.NotificationItem.builder()
			.notificationId(notification.getId())
			.section(notification.getSection())
			.type(notification.getType())
			.content(notification.getContent())
			.imageUrl(notification.getImageUrl())
			.targetType(notification.getTargetType())
			.targetId(notification.getTargetId())
			.isRead(notification.isRead())
			.createdAt(notification.getCreatedAt())
			.build();
	}

	public static NotificationListResponse toListResponse(
		List<NotificationListResponse.NotificationItem> items,
		Long lastId,
		boolean hasNext
	) {
		return NotificationListResponse.builder()
			.notifications(items)
			.lastId(lastId)
			.hasNext(hasNext)
			.build();
	}
}
