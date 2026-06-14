package com.cotato.itda.domain.notification.exception.code;

import org.springframework.http.HttpStatus;

import com.cotato.itda.global.error.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
	NOTIFICATION_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"해당 알림을 찾을 수 없습니다.",
		"NOTIFICATION_ERROR_404_NOTIFICATION_NOT_FOUND"
	),
	NOTIFICATION_FORBIDDEN(
		HttpStatus.FORBIDDEN,
		"해당 알림에 대한 권한이 없습니다.",
		"NOTIFICATION_ERROR_403_NOTIFICATION_FORBIDDEN"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
