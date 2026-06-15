package com.cotato.itda.domain.notification.entity;

import java.time.LocalDateTime;

import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.notification.enums.NotificationSection;
import com.cotato.itda.domain.notification.enums.NotificationTargetType;
import com.cotato.itda.domain.notification.enums.NotificationType;
import com.cotato.itda.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
	name = "notifications",
	indexes = {
		@Index(name = "idx_notifications_receiver_id", columnList = "receiver_id, id"),
		@Index(name = "idx_notifications_receiver_read", columnList = "receiver_id, is_read")
	}
)
public class Notification extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private Member receiver;

	@Enumerated(EnumType.STRING)
	@Column(name = "section", nullable = false, length = 30)
	private NotificationSection section;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 50)
	private NotificationType type;

	@Column(name = "content", nullable = false, length = 255)
	private String content;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", length = 50)
	private NotificationTargetType targetType;

	@Column(name = "target_id")
	private Long targetId;

	@Builder.Default
	@Column(name = "is_read", nullable = false)
	private boolean read = false;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	public boolean isOwnedBy(Long memberId) {
		return receiver != null && memberId != null && memberId.equals(receiver.getId());
	}

	public void markAsRead(LocalDateTime readAt) {
		if (read) {
			return;
		}
		this.read = true;
		this.readAt = readAt;
	}
}
