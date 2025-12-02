package com.cotato.itda.global.error.exception;

import java.util.Map;

import com.cotato.itda.global.error.constant.ErrorCode;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	/**
	 * 우리 서비스에서 정의한 에러 코드 (HttpStatus, Code, Message 정보 포함)
	 * 예) USER_NOT_FOUND, INVALID_PASSWORD
	 */
	private final ErrorCode errorCode;
	private final Map<String, Object> reasons;

	/**
	 * 예외 발생에 대한 추가적인 상세 정보이다.
	 * key: 필드명 또는 상황 코드, value: 상세 메시지
	 */
	public BusinessException(ErrorCode errorCode) {
		this(errorCode, null);
	}

	/**
	 * 전체 생성자: ErrorCode와 상세 정보(reasons)를 포함하여 예외를 생성한다.
	 *
	 * @param errorCode 발생한 비즈니스 에러 코드
	 * @param reasons 예외에 대한 추가 상세 정보 (Map 형태)
	 */
	public BusinessException(ErrorCode errorCode, Map<String, Object> reasons) {
		// 부모 클래스(RuntimeException)의 생성자를 호출한다.
		// errorCode에 정의된 message가 Exception의 기본 메시지로 설정되어 로그에 기록된다.
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.reasons = reasons;
	}
}
