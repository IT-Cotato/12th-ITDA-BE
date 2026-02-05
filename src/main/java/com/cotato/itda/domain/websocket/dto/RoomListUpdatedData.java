package com.cotato.itda.domain.websocket.dto;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.LastMessageType;

import lombok.Builder;

/**
 * 채팅방 목록 갱신 이벤트 데이터
 * - ROOM_LIST_UPDATED 이벤트의 data 필드에 해당
 */
@Builder
public record RoomListUpdatedData(
	LocalDateTime lastMessageAt,
	String lastMessagePreview,
	LastMessageType lastMessageType,
	Long lastMessageId,
	Long lastMessageSeq
) {
}
