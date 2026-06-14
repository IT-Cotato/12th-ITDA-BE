package com.cotato.itda.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.cotato.itda.domain.notification.exception.code.NotificationErrorCode;
import com.cotato.itda.domain.notification.repository.NotificationRepository;
import com.cotato.itda.global.error.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationService notificationService;

	@Test
	void getNotifications_returns_my_notifications_with_cursor_metadata() {
		Notification first = notification(3L, 1L, false);
		Notification second = notification(2L, 1L, true);
		Notification extra = notification(1L, 1L, false);

		when(notificationRepository.findByReceiverIdOrderByIdDesc(any(), any()))
			.thenReturn(List.of(first, second, extra));

		NotificationListResponse response = notificationService.getNotifications(1L, null, 2);

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

		NotificationListResponse response = notificationService.getNotifications(1L, 10L, 20);

		assertThat(response.notifications()).hasSize(1);
		assertThat(response.lastId()).isEqualTo(9L);
		assertThat(response.hasNext()).isFalse();
		verify(notificationRepository).findByReceiverIdAndIdLessThanOrderByIdDesc(any(), any(), any());
	}

	@Test
	void getUnreadCount_counts_only_my_unread_notifications() {
		when(notificationRepository.countByReceiverIdAndReadFalse(1L)).thenReturn(3L);

		UnreadNotificationCountResponse response = notificationService.getUnreadCount(1L);

		assertThat(response.count()).isEqualTo(3L);
	}

	@Test
	void markAsRead_marks_my_notification_as_read() {
		Notification notification = notification(1L, 1L, false);
		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

		notificationService.markAsRead(1L, 1L);

		assertThat(notification.isRead()).isTrue();
		assertThat(notification.getReadAt()).isNotNull();
	}

	@Test
	void markAsRead_keeps_already_read_notification_successful() {
		Notification notification = notification(1L, 1L, true);
		LocalDateTime readAt = LocalDateTime.of(2026, 6, 14, 12, 0);
		ReflectionTestUtils.setField(notification, "readAt", readAt);
		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

		notificationService.markAsRead(1L, 1L);

		assertThat(notification.isRead()).isTrue();
		assertThat(notification.getReadAt()).isEqualTo(readAt);
	}

	@Test
	void markAsRead_rejects_other_members_notification() {
		Notification notification = notification(1L, 2L, false);
		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

		assertThatThrownBy(() -> notificationService.markAsRead(1L, 1L))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(NotificationErrorCode.NOTIFICATION_FORBIDDEN);

		assertThat(notification.isRead()).isFalse();
	}

	@Test
	void markAsRead_rejects_missing_notification() {
		when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> notificationService.markAsRead(1L, 1L))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
	}

	@Test
	void markAllAsRead_updates_only_my_unread_notifications() {
		notificationService.markAllAsRead(1L);

		verify(notificationRepository).markAllAsReadByReceiverId(any(), any());
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
