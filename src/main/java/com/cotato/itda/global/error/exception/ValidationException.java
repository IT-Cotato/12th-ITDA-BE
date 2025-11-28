package com.cotato.itda.global.error.exception;

import java.util.Map;

import com.cotato.itda.global.error.constant.ErrorCode;


/**
 * 유효성 검증(Validation) 실패 시 발생하는 비즈니스 예외이다.
 * * @Valid 또는 @Validated 검증 실패,
 * 혹은 비즈니스 로직 내에서 데이터 유효성 검증 실패 시 사용한다.
 * * BusinessException을 상속받으므로, 에러 코드(errorCode)와 상세 정보(reasons)를 포함할 수 있다.
 * 이 예외는 전역 예외 처리기에서  필드에 필드별 검증 오류 정보를 담아 클라이언트에게 반환할 때 사용된다.
 */
public class ValidationException extends BusinessException {

	public ValidationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ValidationException(ErrorCode errorCode, Map<String, Object> reasons) {
		super(errorCode, reasons);
	}
}