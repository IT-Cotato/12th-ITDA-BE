package com.cotato.itda.domain.notification.service.query;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.notification.converter.NotificationConverter;
import com.cotato.itda.domain.notification.dto.NotificationListResponse;
import com.cotato.itda.domain.notification.dto.UnreadNotificationCountResponse;
import com.cotato.itda.domain.notification.entity.Notification;
import com.cotato.itda.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

	private final NotificationRepository notificationRepository;

	@Override
	public NotificationListResponse getNotifications(Long memberId, Long lastId, int limit) {
		PageRequest pageRequest = PageRequest.of(0, limit + 1);
		List<Notification> fetched = lastId == null
			? notificationRepository.findByReceiverIdOrderByIdDesc(memberId, pageRequest)
			: notificationRepository.findByReceiverIdAndIdLessThanOrderByIdDesc(memberId, lastId, pageRequest);

		boolean hasNext = fetched.size() > limit;
		List<Notification> page = hasNext ? fetched.subList(0, limit) : fetched;
		Long nextLastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();

		List<NotificationListResponse.NotificationItem> items = page.stream()
			.map(NotificationConverter::toListItem)
			.toList();

		return NotificationConverter.toListResponse(items, nextLastId, hasNext);
	}

	@Override
	public UnreadNotificationCountResponse getUnreadCount(Long memberId) {
		return UnreadNotificationCountResponse.builder()
			.count(notificationRepository.countByReceiverIdAndReadFalse(memberId))
			.build();
	}
}
