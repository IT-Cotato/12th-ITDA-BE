package com.cotato.itda.domain.chat.repository.dto;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.LastMessageType;
import com.cotato.itda.domain.chat.enums.RoomType;

/**
 * 내 방 목록 조회 결과를 담는 불변 객체
 * - 엔티티를 그대로 반환하지 않고, 목록에 필요한 칼럼만 뽑아오기 위해 사용
 */
public record MyRoomRow(
	// chat_room fields
	Long roomId,
	RoomType roomType,
	String roomName,

	Long lastMessageId,
	Long lastMessageSeq,
	LocalDateTime lastMessageAt,
	String lastMessagePreview,
	LastMessageType lastMessageType,

	// chat_room_member fields
	long joinSeq,
	long lastReadSeq

) {

}
