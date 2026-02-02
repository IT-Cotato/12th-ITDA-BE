package com.cotato.itda.domain.chat.controller.dto;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.LastMessageType;
import com.cotato.itda.domain.chat.enums.RoomType;

import lombok.Builder;

@Builder
public record ChatRoomListItemDto(
	Long roomId,
	String roomName,
	RoomType roomType,

	Long lastMessageId,
	Long lastMessageSeq,
	LocalDateTime lastMessageAt,
	String lastMessagePreview,
	LastMessageType lastMessageType,

	long unreadCount,


	OpponentSummaryDto opponent
) {
}
