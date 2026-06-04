package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements ErrorCode {

	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", "JWT_401_UNAUTHORIZED"),
	// 401 Unauthorized: 인증 실패

	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 입니다.", "JWT_401_INVALID"),
	LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "이미 로그아웃 처리된 JWT 토큰입니다.", "JWT_401_LOGGED_OUT"),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다.", "JWT_401_EXPIRED"),
	UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다.", "JWT_401_UNSUPPORTED"),
	MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 형식의 JWT 토큰입니다.", "JWT_401_MALFORMED"),
	EMPTY_CLAIMS(HttpStatus.UNAUTHORIZED, "JWT 클레임이 비어있거나 null입니다.", "JWT_401_EMPTY_CLAIMS"),
	TOKEN_ALG_MISMATCH(HttpStatus.UNAUTHORIZED, "토큰 알고리즘이 일치하지 않습니다.", "JWT_401_ALG_MISMATCH"),
	TOKEN_PURPOSE_MISMATCH(HttpStatus.UNAUTHORIZED, "토큰 목적이 일치하지 않습니다.", "JWT_401_PURPOSE_MISMATCH"),
	// 400 Bad Request or 401: 토큰 누락
	TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "헤더에 토큰이 존재하지 않거나 형식이 잘못되었습니다.", "JWT_401_TOKEN_NOT_FOUND"),
	MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다.", "JWT_401_MISSING_TOKEN");
	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

}
