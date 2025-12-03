package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	// 404
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", "USER_ERROR_404_NOT_FOUND"),

	// 400
	DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다.", "USER_ERROR_400_DUPLICATE_EMAIL"),
	DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다.", "USER_ERROR_400_DUPLICATE_NICKNAME"),
	INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다.", "USER_ERROR_400_INVALID_VERIFICATION_CODE"),
	EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "만료된 인증 코드입니다.", "USER_ERROR_400_EXPIRED_VERIFICATION_CODE"),
	SOCIAL_PROVIDER_MISMATCH(HttpStatus.BAD_REQUEST, "다른 소셜 제공자로 이미 가입된 계정입니다.",
		"USER_ERROR_400_SOCIAL_PROVIDER_MISMATCH"),

	// 401
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다.", "USER_ERROR_401_INVALID_PASSWORD"),

	// 403
	INVALID_ACCOUNT_STATUS(HttpStatus.FORBIDDEN, "계정 상태가 유효하지 않습니다.", "USER_ERROR_403_INVALID_ACCOUNT_STATUS"),
	EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 완료되지 않았습니다.", "USER_ERROR_403_EMAIL_NOT_VERIFIED"),
	ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "잠금 처리된 계정입니다.", "USER_ERROR_403_ACCOUNT_LOCKED"),
	WITHDRAWN_USER(HttpStatus.FORBIDDEN, "탈퇴 처리된 계정입니다.", "USER_ERROR_403_WITHDRAWN_USER"),

	// 409
	SOCIAL_ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "이미 다른 계정에 연동된 소셜 계정입니다.",
		"USER_ERROR_409_SOCIAL_ACCOUNT_ALREADY_LINKED"),

	// 429
	LOGIN_ATTEMPT_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도 제한을 초과했습니다.",
		"USER_ERROR_429_LOGIN_ATTEMPT_LIMIT_EXCEEDED");

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}

