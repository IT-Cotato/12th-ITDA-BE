package com.cotato.itda.domain.notification.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.notification.entity.Notification;
import com.cotato.itda.domain.notification.enums.NotificationSection;
import com.cotato.itda.domain.notification.enums.NotificationTargetType;
import com.cotato.itda.domain.notification.enums.NotificationType;
import com.cotato.itda.domain.notification.exception.code.NotificationErrorCode;
import com.cotato.itda.domain.notification.repository.NotificationRepository;
import com.cotato.itda.global.error.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceImplTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationCommandServiceImpl notificationCommandService;

	@Test
	void markAsRead_marks_my_notification_as_read() {
		Notification notification = notification(1L, 1L, false);
		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

		notificationCommandService.markAsRead(1L, 1L);

		assertThat(notification.isRead()).isTrue();
		assertThat(notification.getReadAt()).isNotNull();
	}

	@Test
	void markAsRead_keeps_already_read_notification_successful() {
		Notification notification = notification(1L, 1L, true);
		LocalDateTime readAt = LocalDateTime.of(2026, 6, 14, 12, 0);
		ReflectionTestUtils.setField(notification, "readAt", readAt);
		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

		notificationCommandService.markAsRead(1L, 1L);

		assertThat(notification.isRead()).isTrue();
		assertThat(notification.getReadAt()).isEqualTo(readAt);
	}

	@Test
	void markAsRead_rejects_other_members_notification() {
		Notification notification = notification(1L, 2L, false);
		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

		assertThatThrownBy(() -> notificationCommandService.markAsRead(1L, 1L))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(NotificationErrorCode.NOTIFICATION_FORBIDDEN);

		assertThat(notification.isRead()).isFalse();
	}

	@Test
	void markAsRead_rejects_missing_notification() {
		when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> notificationCommandService.markAsRead(1L, 1L))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
	}

	@Test
	void markAllAsRead_updates_only_my_unread_notifications() {
		notificationCommandService.markAllAsRead(1L);

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
