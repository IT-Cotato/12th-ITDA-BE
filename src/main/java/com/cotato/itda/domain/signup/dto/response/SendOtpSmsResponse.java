package com.cotato.itda.domain.signup.dto.response;

import java.time.OffsetDateTime;

public record SendOtpSmsResponse(
	String step, // "OTP_REQUIRED"
	Flags flags
	) {
	public record Flags(
		boolean canSendSms, // 지금은 SMS 발송이 가능한지 여부
		OffsetDateTime resendAvailableAt, // 다음 SMS 발송이 허용되는 시간
		OffsetDateTime otpExpiresAt, // OTP 만료 시간,
		int smsSendCount, // 지금까지 발송된 SMS 수
		int remainingResendCount, // 남은 재전송 횟수
		int remainingOtpAttempts // "현재 발급된 OTP"에 대한 남은 시도 횟수
	){}
}