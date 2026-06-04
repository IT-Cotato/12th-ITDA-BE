package com.cotato.itda.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(
	@Schema(description = "Refresh Token(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	@NotBlank(message = "refreshToken은 필수입니다.")
	String refreshToken
) {
}
