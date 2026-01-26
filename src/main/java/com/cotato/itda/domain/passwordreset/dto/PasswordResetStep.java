package com.cotato.itda.domain.passwordreset.dto;

public enum PasswordResetStep {
	OTP_REQUIRED, // OTP 발송 필요 상태
	PASSWORD_REQUIRED, // 비밀번호 제출 필요 상태
	PASSWORD_RESET_COMPLETED // 비밀번호 재설정 완료 상태
}
