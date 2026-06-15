package com.cotato.itda.domain.notification.exception;

import java.util.Map;

import com.cotato.itda.domain.notification.exception.code.NotificationErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

public class NotificationException extends BusinessException {

	public NotificationException(NotificationErrorCode errorCode) {
		super(errorCode);
	}

	public NotificationException(NotificationErrorCode errorCode, Map<String, Object> reasons) {
		super(errorCode, reasons);
	}
}
