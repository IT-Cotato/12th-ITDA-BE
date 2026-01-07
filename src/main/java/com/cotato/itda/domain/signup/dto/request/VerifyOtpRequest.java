package com.cotato.itda.domain.signup.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * STEP4 요청 DTO
 * - 클라이언트는 OTP 코드만 보낸다.
 * - phone은 Draft(otp.phone)에 이미 저장되어 있다고 가정한다.
 */
public record VerifyOtpRequest(
	@NotBlank(message = "otpCode는 필수입니다")
	String otpCode
) {}