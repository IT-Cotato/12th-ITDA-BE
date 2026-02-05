package com.cotato.itda.domain.websocket.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 개인 이벤트 채널로 보내는 이벤트 포맷
 * - 프론트가 type으로 분기
 * - data는 이벤트별 record로 내려줌
 */
@Builder
public record ChatEventEnvelope<T> (
	ChatEventType type,
	Long roomId,
	Long opponentMemberId, // DIRECT 일 때 상대 식별용
	LocalDateTime eventAt,
	T data
) {}
