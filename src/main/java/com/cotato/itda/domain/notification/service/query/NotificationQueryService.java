package com.cotato.itda.domain.notification.service.query;

import com.cotato.itda.domain.notification.dto.NotificationListResponse;
import com.cotato.itda.domain.notification.dto.UnreadNotificationCountResponse;

public interface NotificationQueryService {

	NotificationListResponse getNotifications(Long memberId, Long lastId, int limit);

	UnreadNotificationCountResponse getUnreadCount(Long memberId);
}
