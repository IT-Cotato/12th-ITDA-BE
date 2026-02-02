package com.cotato.itda.domain.chat.controller.dto;

import lombok.Builder;

@Builder
public record OpponentSummaryDto (
	Long memberId,
	String name,
	String profileImageUrl
){}
