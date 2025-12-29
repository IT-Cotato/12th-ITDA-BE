package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignupErrorCode implements ErrorCode{

	TERMS_BUNDLE_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"가입 약관 번들을 찾을 수 없습니다.",
		"SIGNUP_ERROR_404_TERMS_BUNDLE_NOT_FOUND"
	),

	TERMS_ITEM_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"가입 약관 항목을 찾을 수 없습니다.",
		"SIGNUP_ERROR_404_TERMS_ITEM_NOT_FOUND"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
