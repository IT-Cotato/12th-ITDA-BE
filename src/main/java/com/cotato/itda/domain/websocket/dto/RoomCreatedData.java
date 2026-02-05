package com.cotato.itda.domain.websocket.dto;

import lombok.Builder;

/**
 * 채팅방 생성 이벤트 데이터
 * - ROOM_CREATED 이벤트의 data 필드에 해당
 */
@Builder
public record RoomCreatedData(
	Long firstMessageId,
	long firstMessageSeq,
	String lastMessagePreview
){}
