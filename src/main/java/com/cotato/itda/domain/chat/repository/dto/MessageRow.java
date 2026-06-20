package com.cotato.itda.domain.chat.repository.dto;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.AttachmentStatus;
import com.cotato.itda.domain.chat.enums.AttachmentType;
import com.cotato.itda.domain.chat.enums.MessageType;

/**
 * 히스토리 조회용 row (메시지 + 첨부 join + 답장 원본 메시지 join)
 * - attachment는 없을 수 있으니 nullable
 */
public record MessageRow (
	long messageId,
	long messageSeq,
	Long senderId,
	MessageType messageType,
	String content,
	LocalDateTime createdAt,

	AttachmentType attachmentType,
	String objectKey,
	String mimeType,
	Long sizeBytes,
	AttachmentStatus attachmentStatus,
	Integer durationMs,

    Long parentMessageId,
    Long parentSenderId,
    String parentSenderNickname,
    MessageType parentMessageType,
    String parentContent,
    AttachmentType parentAttachmentType
	){}
