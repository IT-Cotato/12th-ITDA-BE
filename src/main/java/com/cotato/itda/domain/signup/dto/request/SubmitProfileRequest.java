package com.cotato.itda.domain.signup.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * {
 * "name": "김기민",
 * "birthDate": "2000-02-26"
 * }
 */
public record SubmitProfileRequest(
	@NotBlank(message = "name is required")
	String name,

	@NotNull(message = "birthDate is required")
	LocalDate birthDate
) {
}
