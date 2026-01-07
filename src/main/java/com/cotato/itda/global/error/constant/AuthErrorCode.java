package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode{

	INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "AUTH_400_001", "전화번호 형식이 올바르지 않습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "전화번호 또는 비밀번호가 올바르지 않습니다.");

	/**
	 * HTTP 상태 코드 (예: 400, 401, 403, 404, 500...)
	 * 클라이언트/게이트웨이에서 공통적으로 이해하는 표준 상태 코드.
	 */
	private final HttpStatus httpStatus;

	/**
	 * 우리 서비스 내부에서 쓰는 식별용 에러 코드.
	 * - 로그/모니터링/프론트 분기에서 사용
	 * - message는 나중에 바뀌어도 되지만, code는 되도록 안정적으로 유지
	 */
	private final String code;

	/**
	 * 사용자/클라이언트에게 노출할 메시지.
	 * - 기본 한국어 설명
	 * - 나중에 i18n(다국어) 적용 시 이 필드를 locale에 맞게 변환해서 내려줄 수 있음
	 */
	private final String message;
}
