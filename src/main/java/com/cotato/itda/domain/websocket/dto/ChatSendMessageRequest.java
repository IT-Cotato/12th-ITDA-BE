package com.cotato.itda.domain.websocket.dto;

import com.cotato.itda.domain.chat.controller.dto.ChatAttachmentRequest;
import com.cotato.itda.domain.chat.enums.MessageType;

import jakarta.validation.constraints.NotNull;

/**
 * 임시 화면(roomId 없음)에서도 보내야 하므로 roomId는 nullable
 * - clientMessageId: 클라이언트에서 생성한 메시지 ID (중복 방지 및 추적용)
 * TEXT
 * - content 필수
 * - attachment null
 * ATTACHMENT (S3 업로드 완료 후)
 * - content null
 * - attachment 필수
 * - attachment.objectKey 필수
 */
public record ChatSendMessageRequest(
	@NotNull String clientMessageId,
	Long roomId,
	Long opponentMemberId, // roomId가 없을 때 필수
	@NotNull MessageType messageType,
	String content,
	ChatAttachmentRequest attachment
	){
}
