package com.cotato.itda.global.error.exception;

import org.springframework.security.core.AuthenticationException;

import com.cotato.itda.global.error.constant.JwtErrorCode;

public class JwtAuthenticationException extends AuthenticationException {
	private final JwtErrorCode errorCode;

	public JwtAuthenticationException(JwtErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public JwtErrorCode getErrorCode() {
		return errorCode;
	}
}
