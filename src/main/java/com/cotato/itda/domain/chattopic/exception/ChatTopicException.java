package com.cotato.itda.domain.chattopic.exception;

import com.cotato.itda.domain.chattopic.exception.code.ChatTopicErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import java.util.Map;

public class ChatTopicException extends BusinessException {
    public ChatTopicException(ChatTopicErrorCode errorCode) {
        super(errorCode);
    }

    public ChatTopicException(ChatTopicErrorCode errorCode, Map<String, Object> reasons) {
        super(errorCode, reasons);
    }
}
