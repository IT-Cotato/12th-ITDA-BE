package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisErrorCode implements ErrorCode{

	SERIALIZATION_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"Redis 직렬화/역직렬화 오류가 발생했습니다.",
		"REDIS_ERROR_500_SERIALIZATION_ERROR"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
