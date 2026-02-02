package com.cotato.itda.domain.chat.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record ChatRoomSliceResponse (
	List<ChatRoomListItemDto> items,
	boolean hasNext,
	LocalDateTime nextCursorAt,
	Long nextCursorRoomId
	){
}
