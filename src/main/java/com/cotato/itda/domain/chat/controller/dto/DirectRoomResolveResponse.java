package com.cotato.itda.domain.chat.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DIRECT 방 존재 여부 resolve 결과
 */
public record DirectRoomResolveResponse(

	@Schema(description = "방 존재 여부", example="true")
	boolean exists,

	@Schema(description = "존재하는 경우 해당 방 ID(없으면 null)", example = "42", nullable = true)
	Long roomId
) {
}
