package com.cotato.itda.domain.chat.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "ChatRoomSliceResponse", description = "내 채팅방 목록 조회 응답 (Slice 기반 커서 페이지네이션)")
@Builder
public record ChatRoomSliceResponse(

	@Schema(description = "채팅방 목록 아이템들")
	List<ChatRoomListItemDto> items,

	@Schema(description = "다음 페이지 존재 여부", example = "true")
	boolean hasNext,

	@Schema(description = "다음 페이지 커서 시간 (hasNext=false면 null)", example = "2026-02-02T14:30:15.123")
	LocalDateTime nextCursorAt,

	@Schema(description = "다음 페이지 커서 방 ID (hasNext=false면 null)", example = "50")
	Long nextCursorRoomId
) {}