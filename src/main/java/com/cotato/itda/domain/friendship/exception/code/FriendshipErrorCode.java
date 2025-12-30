package com.cotato.itda.domain.friendship.exception.code;

import com.cotato.itda.global.error.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FriendshipErrorCode implements ErrorCode {

    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FRIENDSHIP_ERROR_404_NOT_FOUND",
            "친구 관계를 찾을 수 없습니다."),

    INVALID_SPEECH_STYLE(
            HttpStatus.BAD_REQUEST,
            "FRIENDSHIP_ERROR_400_INVALID_SPEECH_STYLE",
            "유효하지 않은 대화 말투입니다."
    ),

    CANNOT_ADD_SELF(
            HttpStatus.BAD_REQUEST,
            "FRIENDSHIP_ERROR_400_CANNOT_ADD_SELF",
            "자기 자신을 친구로 추가할 수 없습니다."
    ),

    FRIENDSHIP_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "FRIENDSHIP_ERROR_400_FRIENDSHIP_ALREADY_EXISTS",
            "이미 존재하는 친구 관계입니다."
    ),

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "FRIENDSHIP_ERROR_403_FORBIDDEN",
            "권한이 없는 친구 관계입니다."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
