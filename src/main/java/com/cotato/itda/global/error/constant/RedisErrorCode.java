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
	),
	DRAFT_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"Redis에서 해당 가입 임시 저장 데이터를 찾을 수 없습니다.",
		"REDIS_ERROR_404_DRAFT_NOT_FOUND"
	),

	INVALID_TTL(
		HttpStatus.BAD_REQUEST,
		"Redis TTL(만료 시간)이 유효하지 않습니다.",
		"REDIS_ERROR_400_INVALID_TTL"
	);
	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
