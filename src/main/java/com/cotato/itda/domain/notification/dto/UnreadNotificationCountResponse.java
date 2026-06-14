package com.cotato.itda.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UnreadNotificationCountResponse(
	@Schema(description = "읽지 않은 알림 개수", example = "3")
	long count
) {
}
