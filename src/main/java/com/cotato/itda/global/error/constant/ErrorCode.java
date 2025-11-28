package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String getCode();

	String getMessage();

	HttpStatus getHttpStatus();
}
