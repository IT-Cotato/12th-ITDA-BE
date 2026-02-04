package com.cotato.itda.domain.chat.repository.dto;

// 대화 상대방 정보를 담는 불변 객체
public record OpponentRow(
	Long roomId,
	Long memberId,
	String name,
	String profileImageUrl
) {
}
