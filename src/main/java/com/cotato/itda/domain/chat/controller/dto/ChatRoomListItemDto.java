package com.cotato.itda.domain.chat.controller.dto;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.LastMessageType;
import com.cotato.itda.domain.chat.enums.RoomType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
@Schema(name = "ChatRoomListItemDto", description = "채팅방 목록 아이템")
@Builder
public record ChatRoomListItemDto(

	@Schema(description = "채팅방 ID", example = "50")
	Long roomId,

	@Schema(description = "채팅방 이름", example = "기민님과의 대화")
	String roomName,

	@Schema(description = "친구 관계 ID", example = "123")
	Long friendShipId,

	@Schema(description = "채팅방 타입", example = "DIRECT")
	RoomType roomType,

	@Schema(description = "마지막 메시지 ID", example = "999")
	Long lastMessageId,

	@Schema(description = "마지막 메시지 시퀀스", example = "120")
	Long lastMessageSeq,

	@Schema(description = "마지막 메시지 생성 시각", example = "2026-02-02T14:30:15.123")
	LocalDateTime lastMessageAt,

	@Schema(description = "마지막 메시지 프리뷰", example = "안녕하세요!")
	String lastMessagePreview,

	@Schema(description = "마지막 메시지 타입", example = "TEXT")
	LastMessageType lastMessageType,

	@Schema(description = "안 읽은 메시지 수", example = "3")
	long unreadCount,

	@Schema(description = "DIRECT일 때 상대 정보 (GROUP이면 null)")
	OpponentSummaryDto opponent
) {}