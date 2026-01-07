package com.cotato.itda.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
	@Schema(description = "휴대폰 번호(숫자만). 예: 01012345678", example = "01012345678")
	@NotBlank(message = "phoneNumber는 필수입니다.")
	String phoneNumber,

	@Schema(description = "비밀번호", example = "P@ssw0rd!")
	@NotBlank(message = "password는 필수입니다.")
	String password
) {
}
