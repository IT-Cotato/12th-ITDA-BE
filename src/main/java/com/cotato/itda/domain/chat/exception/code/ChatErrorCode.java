package com.cotato.itda.domain.chat.exception.code;

import org.springframework.http.HttpStatus;

import com.cotato.itda.global.error.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

	INVALID_MESSAGE_CONTENT(
		HttpStatus.BAD_REQUEST,
		"메시지 내용이 유효하지 않습니다.",
		"CHAT_ERROR_400_INVALID_MESSAGE_CONTENT"
	),
	INVALID_ATTACHMENT_FOR_TEXT(
		HttpStatus.BAD_REQUEST,
		"텍스트 메시지에는 첨부파일이 포함될 수 없습니다.",
		"CHAT_ERROR_400_INVALID_ATTACHMENT_FOR_TEXT"
	),
	INVALID_ATTACHMENT_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"첨부파일 메시지에는 첨부파일이 반드시 포함되어야 합니다.",
		"CHAT_ERROR_400_INVALID_ATTACHMENT_REQUIRED"
	),
	INVALID_ATTACHMENT_OBJECT_KEY(
		HttpStatus.BAD_REQUEST,
		"첨부파일의 objectKey가 유효하지 않습니다.",
		"CHAT_ERROR_400_INVALID_ATTACHMENT_OBJECT_KEY"
	),
	// 현재 상태가 ACTIVE가 아닙니다
	CHAT_ROOM_MEMBER_STATUS_INVALID(
		HttpStatus.BAD_REQUEST,
		"현재 채팅 방 멤버의 상태가 유효하지 않습니다.",
		"CHAT_ERROR_400_ROOM_MEMBER_STATUS_INVALID"
	),


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
