package com.cotato.itda.domain.chat.exception.code;

import org.springframework.http.HttpStatus;

import com.cotato.itda.global.error.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

	INVALID_REQUEST_BOTH_ROOMID_OPPONENTID_NULL(
		HttpStatus.BAD_REQUEST,
		"roomId와 opponentMemberId가 모두 null일 수 없습니다.",
		"CHAT_ERROR_400_INVALID_REQUEST_BOTH_ROOMID_OPPONENTID_NULL"
	),
	INVALID_CURSOR(
		HttpStatus.BAD_REQUEST,
		"잘못된 커서입니다.",
		"CHAT_ERROR_400_INVALID_CURSOR"
	),

	// 쫓겨난 상태에서는 다시 방에 참여할 수 없음
	CHAT_ROOM_MEMBER_CREATE_FORBIDDEN(
		HttpStatus.FORBIDDEN,
		"채팅 방에 참여할 수 있는 권한이 없습니다.",
		"CHAT_ERROR_403_ROOM_MEMBER_CREATE_FORBIDDEN"
	),
	CHAT_ROOM_MEMBER_CREATE_FAIL(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"채팅 방 멤버 생성에 실패했습니다.",
		"CHAT_ERROR_500_ROOM_MEMBER_CREATE_FAIL"
	),
	// member null 불가
	CHAT_MEMBER_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"채팅 멤버를 찾을 수 없습니다.",
		"CHAT_ERROR_404_MEMBER_NOT_FOUND"
	),
	// room null 불가
	CHAT_ROOM_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"채팅 방을 찾을 수 없습니다.",
		"CHAT_ERROR_404_ROOM_NOT_FOUND"
	),
	// chat message null 불가
	CHAT_MESSAGE_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"채팅 메시지를 찾을 수 없습니다.",
		"CHAT_ERROR_404_MESSAGE_NOT_FOUND"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
