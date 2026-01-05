package com.cotato.itda.domain.signup.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitPasswordRequest (
	@NotBlank(message= "password is required")
	String password,
	@NotBlank(message= "confirmPassword is required")
	String confirmPassword
){
}
