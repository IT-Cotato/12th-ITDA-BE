package com.cotato.itda.domain.chat.entity;

import com.cotato.itda.domain.chat.enums.AttachmentStatus;
import com.cotato.itda.domain.chat.enums.AttachmentType;
import com.cotato.itda.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message_attachment",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_chat_attachment_message", columnNames = {"chat_message_id"})
	}
)
@Entity
public class ChatMessageAttachment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// FK : chat_message_id -> chat_message.id
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_message_id", nullable = false)
	private ChatMessage message;

	@Enumerated(EnumType.STRING)
	@Column(name = "attachment_type", nullable = false, length = 50)
	private AttachmentType attachmentType;

	// S3에 저장된 파일의 키 값
	// 예: "attachments/2023/10/15/unique-file-name.jpg"
	// 이 값을 사용하여 S3에서 파일을 조회하거나 삭제할 수 있다
	@Column(name = "object_key", nullable=false,length=1024)
	private String objectKey;

	// MIME 타입
	// ex: "image/jpeg", "audio/mpeg"
	@Column(name = "mime_type", length = 100)
	private String mimeType;

	// 파일 크기
	// ex: 204800 (bytes)
	@Column(name = "size_bytes")
	private Long sizeBytes;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private AttachmentStatus status;

	// 음성 파일의 재생 시간 (밀리초 단위)
	// ex: 15000 (15초)
	@Column(name = "duration_ms")
	private Integer durationMs;

	@Builder
	private ChatMessageAttachment(
		ChatMessage message,
		AttachmentType attachmentType,
		String objectKey,
		String mimeType,
		Integer sizeBytes,
		AttachmentStatus status,
		Integer durationMs
	) {
		this.message = message;
		this.attachmentType = attachmentType;
		this.objectKey = objectKey;
		this.mimeType = mimeType;
		this.sizeBytes = sizeBytes;
		this.status = status;
		this.durationMs = durationMs;
	}

	public void markReady() {
		this.status = AttachmentStatus.READY;
	}

	public void markFailed() {
		this.status = AttachmentStatus.FAILED;
	}
}
