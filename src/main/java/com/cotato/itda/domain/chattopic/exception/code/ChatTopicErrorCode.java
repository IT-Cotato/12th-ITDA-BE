package com.cotato.itda.domain.chattopic.exception.code;

import com.cotato.itda.global.error.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatTopicErrorCode implements ErrorCode {

    CHAT_TOPIC_TEMPLATE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHAT_TOPIC_ERROR_404_CHAT_TOPIC_TEMPLATE_NOT_FOUND",
            "대화 주제 템플릿을 찾을 수 없습니다."
    ),
    CHAT_TOPIC_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHAT_TOPIC_ERROR_404_CHAT_TOPIC_NOT_FOUND",
            "대화 주제를 찾을 수 없습니다."
    ),
    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHAT_TOPIC_ERROR_404_NOT_FOUND",
            "대화 주제를 찾을 수 없습니다."
    ),

    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "CHAT_TOPIC_ERROR_400_BAD_REQUEST",
            "유효하지 않은 요청입니다."
    )
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
