package com.cotato.itda.domain.websocket.dto;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.AttachmentStatus;
import com.cotato.itda.domain.chat.enums.AttachmentType;
import com.cotato.itda.domain.chat.enums.MessageType;

import lombok.Builder;

/**
 * 방 토픽(/topic/chat/room/{roomId})으로 보내는 실시간 타임라인 메시지
 */
@Builder
public record ChatRoomMessageDto(
	Long roomId,
	Long messageId,
	long messageSeq,
	Long senderMemberId,
	MessageType messageType,
	String content,
	LocalDateTime createdAt,
	Attachment attachment
) {
	@Builder
	public record Attachment(
		AttachmentType attachmentType,
		String objectKey,
		String mimeType,
		Integer sizeBytes,
		AttachmentStatus status,
		Integer durationMs
	) {}
}