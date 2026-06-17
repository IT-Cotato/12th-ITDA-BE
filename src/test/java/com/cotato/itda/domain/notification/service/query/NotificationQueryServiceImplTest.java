package com.cotato.itda.domain.notification.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.notification.dto.NotificationListResponse;
import com.cotato.itda.domain.notification.dto.UnreadNotificationCountResponse;
import com.cotato.itda.domain.notification.entity.Notification;
import com.cotato.itda.domain.notification.enums.NotificationSection;
import com.cotato.itda.domain.notification.enums.NotificationTargetType;
import com.cotato.itda.domain.notification.enums.NotificationType;
import com.cotato.itda.domain.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceImplTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationQueryServiceImpl notificationQueryService;

	@Test
	void getNotifications_returns_my_notifications_with_cursor_metadata() {
		Notification first = notification(3L, 1L, false);
		Notification second = notification(2L, 1L, true);
		Notification extra = notification(1L, 1L, false);

		when(notificationRepository.findByReceiverIdOrderByIdDesc(any(), any()))
			.thenReturn(List.of(first, second, extra));

		NotificationListResponse response = notificationQueryService.getNotifications(1L, null, 2);

		assertThat(response.notifications()).hasSize(2);
		assertThat(response.notifications().get(0).notificationId()).isEqualTo(3L);
		assertThat(response.notifications().get(0).section()).isEqualTo(NotificationSection.GARDEN);
		assertThat(response.notifications().get(0).type()).isEqualTo(NotificationType.PLANT_INVITE);
		assertThat(response.lastId()).isEqualTo(2L);
		assertThat(response.hasNext()).isTrue();

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(notificationRepository).findByReceiverIdOrderByIdDesc(any(), pageableCaptor.capture());
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(3);
	}

	@Test
	void getNotifications_uses_last_id_cursor_when_present() {
		when(notificationRepository.findByReceiverIdAndIdLessThanOrderByIdDesc(any(), any(), any()))
			.thenReturn(List.of(notification(9L, 1L, false)));

		NotificationListResponse response = notificationQueryService.getNotifications(1L, 10L, 20);

		assertThat(response.notifications()).hasSize(1);
		assertThat(response.lastId()).isEqualTo(9L);
		assertThat(response.hasNext()).isFalse();
		verify(notificationRepository).findByReceiverIdAndIdLessThanOrderByIdDesc(any(), any(), any());
	}

	@Test
	void getUnreadCount_counts_only_my_unread_notifications() {
		when(notificationRepository.countByReceiverIdAndReadFalse(1L)).thenReturn(3L);

		UnreadNotificationCountResponse response = notificationQueryService.getUnreadCount(1L);

		assertThat(response.count()).isEqualTo(3L);
	}

	private Notification notification(Long notificationId, Long receiverId, boolean isRead) {
		Member receiver = Member.builder().build();
		ReflectionTestUtils.setField(receiver, "id", receiverId);

		Notification notification = Notification.builder()
			.receiver(receiver)
			.section(NotificationSection.GARDEN)
			.type(NotificationType.PLANT_INVITE)
			.content("초대장이 도착했어요.")
			.imageUrl("https://example.com/profile.jpg")
			.targetType(NotificationTargetType.SHARED_PLANT_INVITE)
			.targetId(10L)
			.read(isRead)
			.build();

		ReflectionTestUtils.setField(notification, "id", notificationId);
		ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.of(2026, 6, 14, 12, 0));
		return notification;
	}
}
