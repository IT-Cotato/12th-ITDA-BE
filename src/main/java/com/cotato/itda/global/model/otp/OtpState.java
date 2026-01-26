package com.cotato.itda.global.model.otp;

import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record OtpState(
	String phone,                  // 사용자가 입력한 원본(표준화된 형태 권장: 01012345678)

	boolean canSendSms,            // Step2 직후 true, Step3 직후 false
	int smsSendCount,              // Step3 호출 횟수
	int remainingOtpAttempts,      // “현재 발급된 OTP”에 대한 남은 시도 횟수 (예: 3)

	int remainingResendCount,     // 남은 재전송 횟수 (예: 3)
	//레디스에 저장할때는 OffsetDateTime 형태로 저장
	OffsetDateTime resendAvailableAt, // 재전송 가능 시각
	OffsetDateTime otpExpiresAt,       // OTP 만료 시각
	String otpCodeHash,            // OTP 코드 해시값 (STEP3에서 채워짐 -> STEP4에서 검증)

	boolean phoneVerified,         // Step4 성공 시 true
	OffsetDateTime verifiedAt    // Step4 성공 시각
) {
}
