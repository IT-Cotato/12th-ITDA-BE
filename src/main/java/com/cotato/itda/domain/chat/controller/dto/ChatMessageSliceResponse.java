package com.cotato.itda.domain.chat.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 히스토리 커서 페이지네이션
 * - cursorSeq 기준으로 과거로 내려가는 방식
 */
@Builder
public record ChatMessageSliceResponse (
	@Schema(description = "메시지 목록(최신 -> 과거순)")
	List<ChatMessageItemDto> items,

	@Schema(description = "다음 페이지 존재 여부")
	boolean hasNext,

	@Schema(description= "다음 커서(message_seq). 다음 요청에서 cursorSeq로 넘김", nullable = true)
	Long nextCursorSeq
) {}
