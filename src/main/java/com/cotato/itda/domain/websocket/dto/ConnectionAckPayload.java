package com.cotato.itda.domain.websocket.dto;

public record ConnectionAckPayload(
	String connectedAt,
	String sessionId,
	String principalName,
	String message
) {
}