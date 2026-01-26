package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PasswordResetErrorCode implements ErrorCode {

	MEMBER_ID_NOT_SET(
		HttpStatus.BAD_REQUEST,
		"사용자 ID가 설정되지 않았습니다.",
		"PASSWORD_RESET_ERROR_400_MEMBER_ID_NOT_SET"
	),
	MEMBER_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"해당하는 사용자를 찾을 수 없습니다.",
		"PASSWORD_RESET_ERROR_404_MEMBER_NOT_FOUND"
	),
	// ----------------------------
	// OTP / SMS
	// ----------------------------
	OTP_STATE_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"인증번호 상태 정보를 찾을 수 없습니다.",
		"PASSWORD_RESET_ERROR_404_OTP_STATE_NOT_FOUND"
	),
	OTP_PHONE_NUMBER_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"핸드폰 번호는 필수 입력입니다.",
		"PASSWORD_RESET_ERROR_400_OTP_PHONE_NUMBER_REQUIRED"
	),
	OTP_SMS_SEND_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"인증번호 발송에 실패했습니다.",
		"PASSWORD_RESET_ERROR_500_OTP_SMS_SEND_FAILED"
	),
	OTP_RESEND_NOT_AVAILABLE_YET(
		HttpStatus.BAD_REQUEST,
		"지금은 인증번호 재전송이 불가능합니다. 잠시 후 다시 시도해주세요.",
		"PASSWORD_RESET_ERROR_400_OTP_RESEND_NOT_AVAILABLE_YET"
	),
	OTP_RESEND_LIMIT_EXCEEDED(
		HttpStatus.BAD_REQUEST,
		"인증번호 재전송 시도 횟수를 초과했습니다.",
		"PASSWORD_RESET_ERROR_400_OTP_RESEND_LIMIT_EXCEEDED"
	),
	OTP_EXPIRED(
		HttpStatus.BAD_REQUEST,
		"인증번호가 만료되었습니다. 다시 요청해주세요.",
		"PASSWORD_RESET_ERROR_400_OTP_EXPIRED"
	),
	OTP_INVALID(
		HttpStatus.BAD_REQUEST,
		"인증번호가 올바르지 않습니다.",
		"PASSWORD_RESET_ERROR_400_OTP_INVALID"
	),
	OTP_ATTEMPTS_EXCEEDED(
		HttpStatus.BAD_REQUEST,
		"인증번호 입력 시도 횟수를 초과했습니다. 다시 요청해주세요.",
		"PASSWORD_RESET_ERROR_400_OTP_ATTEMPTS_EXCEEDED"
	),

	// ----------------------------
	// Draft / Step
	// ----------------------------
	PASSWORD_RESET_DRAFT_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"비밀번호 재설정 진행 정보를 찾을 수 없습니다.",
		"PASSWORD_RESET_ERROR_404_PASSWORD_RESET_DRAFT_NOT_FOUND"
	),
	INVALID_PASSWORD_RESET_STEP(
		HttpStatus.BAD_REQUEST,
		"현재 단계에서 허용되지 않는 작업입니다.",
		"PASSWORD_RESET_ERROR_400_INVALID_PASSWORD_RESET_STEP"
	),
	PHONE_NOT_VERIFIED(
		HttpStatus.BAD_REQUEST,
		"전화번호 인증이 필요합니다.",
		"PASSWORD_RESET_ERROR_400_PHONE_NOT_VERIFIED"
	),

	// ----------------------------
	// Password
	// ----------------------------
	PASSWORD_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"비밀번호가 필요합니다.",
		"PASSWORD_RESET_ERROR_400_PASSWORD_REQUIRED"
	),
	PASSWORD_MISMATCH(
		HttpStatus.BAD_REQUEST,
		"비밀번호와 비밀번호 확인이 일치하지 않습니다.",
		"PASSWORD_RESET_ERROR_400_PASSWORD_MISMATCH"
	),
	INVALID_PASSWORD_POLICY(
		HttpStatus.BAD_REQUEST,
		"비밀번호가 정책에 맞지 않습니다.",
		"PASSWORD_RESET_ERROR_400_INVALID_PASSWORD_POLICY"
	),
	PASSWORD_REUSE_NOT_ALLOWED(
		HttpStatus.BAD_REQUEST,
		"기존 비밀번호와 동일하게 설정할 수 없습니다.",
		"PASSWORD_RESET_ERROR_400_PASSWORD_REUSE_NOT_ALLOWED"
	),

	// ----------------------------
	// Abuse / Common
	// ----------------------------
	RATE_LIMIT_EXCEEDED(
		HttpStatus.TOO_MANY_REQUESTS,
		"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
		"PASSWORD_RESET_ERROR_429_RATE_LIMIT_EXCEEDED"
	),

	// ----------------------------
	// Security / Account state (계정 열거 방지용 중립 메시지)
	// ----------------------------
	ACCOUNT_NOT_AVAILABLE(
		HttpStatus.BAD_REQUEST,
		"비밀번호를 재설정할 수 없습니다. 인증 정보를 확인해주세요.",
		"PASSWORD_RESET_ERROR_400_ACCOUNT_NOT_AVAILABLE"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}