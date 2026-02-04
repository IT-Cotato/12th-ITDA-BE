package com.cotato.itda.domain.websocket.dto;

public record WsErrorPayload(
	String timestamp,
	String code,
	String message
) {}
