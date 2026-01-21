package com.cotato.itda.global.error.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChallengeErrorCode implements ErrorCode{

    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "미션이 존재하지 않습니다.", "CHALLENGE_ERROR_404_MISSION_NOT_FOUND"),
    MISSION_DATE_MISMATCH(HttpStatus.BAD_REQUEST, "오늘 날짜의 미션만 참여할 수 있습니다.", "CHALLENGE_ERROR_400_MISSION_DATE_MISMATCH"),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "이미 해당 미션에 참여했습니다.", "CHALLENGE_ERROR_409_ALREADY_PARTICIPATED")
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
