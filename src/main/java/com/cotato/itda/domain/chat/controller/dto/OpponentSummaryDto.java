package com.cotato.itda.domain.chat.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "OpponentSummaryDto", description = "DIRECT 채팅방에서 상대방 요약 정보")
@Builder
public record OpponentSummaryDto(

	@Schema(description = "상대 멤버 ID", example = "123")
	Long memberId,

	@Schema(description = "상대 표시 이름", example = "기민")
	String name,

	@Schema(description = "상대 프로필 이미지 URL", example = "https://cdn.example.com/profiles/123.png")
	String profileImageUrl
) {}