package com.cotato.itda.global.error.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChallengeErrorCode implements ErrorCode{

    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "미션이 존재하지 않습니다.", "CHALLENGE_ERROR_404_MISSION_NOT_FOUND")

    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
