package com.cotato.itda.domain.passwordreset.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * STEP 3 요청 DTO
 * - phoneNumber: OTP 발송할 핸드폰 번호
 */
public record SendOtpRequest(
	@NotBlank(message="핸드폰 번호 입력은 필수입니다") String phoneNumber,
	@NotBlank(message="이름 입력은 필수입니다") String name
) {
}
