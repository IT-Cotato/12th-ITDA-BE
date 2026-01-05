package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignupErrorCode implements ErrorCode{

	OTP_SMS_SEND_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"OTP 문자 발송에 실패했습니다.",
		"SIGNUP_ERROR_500_OTP_SMS_SEND_FAILED"
	),
	PASSWORD_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"비밀번호가 필요합니다.",
		"SIGNUP_ERROR_400_PASSWORD_REQUIRED"
	),
	EXTRA_CONSENT_ITEMS(
		HttpStatus.BAD_REQUEST,
		"존재하지 않는 동의 항목이 포함되어 있습니다.",
		"SIGNUP_ERROR_400_EXTRA_CONSENT_ITEMS"
	),
	MISSING_CONSENT_ITEMS(
		HttpStatus.BAD_REQUEST,
		"동의 항목이 누락되었습니다.",
		"SIGNUP_ERROR_400_MISSIONG_CONSENT_ITEMS"
	),
	TOKEN_ISSUANCE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"토큰 발급에 실패했습니다.",
		"SIGNUP_ERROR_500_TOKEN_ISSUANCE_FAILED"
	),
	INVALID_PASSWORD_POLICY(
		HttpStatus.BAD_REQUEST,
		"비밀번호가 정책에 맞지 않습니다.",
		"SIGNUP_ERROR_400_INVALID_PASSWORD_POLICY"
	),
	MEMBER_ALREADY_EXISTS(
		HttpStatus.CONFLICT,
		"이미 존재하는 회원입니다.",
		"SIGNUP_ERROR_409_MEMBER_ALREADY_EXISTS"
	),
	PASSWORD_MISMATCH(
		HttpStatus.BAD_REQUEST,
		"비밀번호와 비밀번호 확인이 일치하지 않습니다.",
		"SIGNUP_ERROR_400_PASSWORD_MISMATCH"
	),
	MEMBER_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"회원을 찾을 수 없습니다.",
		"SIGNUP_ERROR_404_MEMBER_NOT_FOUND"
	),

	INVALID_PROFILE_BIRTHDATE(
		HttpStatus.BAD_REQUEST,
		"생년월일이 유효하지 않습니다.",
		"SIGNUP_ERROR_400_INVALID_PROFILE_BIRTHDATE"
	),
	INVALID_PROFILE_NAME(
		HttpStatus.BAD_REQUEST,
		"이름이 유효하지 않습니다.",
		"SIGNUP_ERROR_400_INVALID_PROFILE_NAME"
	),
	OTP_PHONE_NUMBER_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"핸드폰 번호는 필수 입력입니다.",
		"SIGNUP_ERROR_400_OTP_PHONE_NUMBER_REQUIRED"
	),
	OTP_RESEND_NOT_AVAILABLE_YET(
		HttpStatus.BAD_REQUEST,
		"지금은 OTP 재전송이 불가능합니다. 잠시 후 다시 시도해주세요.",
		"SIGNUP_ERROR_400_OTP_RESEND_NOT_AVAILABLE_YET"
	),
	OTP_RESEND_LIMIT_EXCEEDED(
		HttpStatus.BAD_REQUEST,
		"OTP 재전송 시도 횟수를 초과했습니다.",
		"SIGNUP_ERROR_400_OTP_RESEND_LIMIT_EXCEEDED"
	),
	SIGNUP_DRAFT_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"회원가입 임시 저장소를 찾을 수 없습니다.",
		"SIGNUP_ERROR_404_SIGNUP_DRAFT_NOT_FOUND"
	),
	DUPLICATE_CONSENT_CODE(
		HttpStatus.BAD_REQUEST,
		"동의 항목 코드가 중복되었습니다.",
		"SIGNUP_ERROR_400_DUPLICATE_CONSENT_CODE"
	),
	TERMS_BUNDLE_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"가입 약관 번들을 찾을 수 없습니다.",
		"SIGNUP_ERROR_404_TERMS_BUNDLE_NOT_FOUND"
	),

	INVALID_TERMS_BUNDLE_PARAMETERS(
		HttpStatus.BAD_REQUEST,
		"약관 번들 유형 또는 버전이 유효하지 않습니다.",
		"SIGNUP_ERROR_400_INVALID_TERMS_BUNDLE_PARAMETERS"
	),
	INVALID_SIGNUP_STEP(
		HttpStatus.BAD_REQUEST,
		"현재 회원가입 단계에서 허용되지 않는 작업입니다.",
		"SIGNUP_ERROR_400_INVALID_SIGNUP_STEP"
	),
	 MISSING_REQUIRED_CONSENT(
		HttpStatus.BAD_REQUEST,
		"필수 동의 항목에 대한 동의가 누락되었습니다.",
		"SIGNUP_ERROR_400_MISSING_REQUIRED_CONSENT"
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
