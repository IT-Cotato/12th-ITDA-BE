package com.cotato.itda.global.error.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChallengeErrorCode implements ErrorCode{

    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "미션이 존재하지 않습니다.", "CHALLENGE_ERROR_404_MISSION_NOT_FOUND"),
    MISSION_DATE_MISMATCH(HttpStatus.BAD_REQUEST, "오늘 날짜의 미션만 참여할 수 있습니다.", "CHALLENGE_ERROR_400_MISSION_DATE_MISMATCH"),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "이미 해당 미션에 참여했습니다.", "CHALLENGE_ERROR_409_ALREADY_PARTICIPATED"),

    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 챌린지가 존재하지 않습니다.", "CHALLENGE_ERROR_404_CHALLENGE_NOT_FOUND"),
    CHALLENGE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 챌린지에 대한 권한이 없습니다.", "CHALLENGE_ERROR_403_CHALLENGE_FORBIDDEN"),

    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 좋아요 누른 일기입니다.", "CHALLENGE_ERROR_409_LIKE_ALREADY_EXISTS"),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "취소할 좋아요가 없습니다.", "CHALLENGE_ERROR_404_LIKE_NOT_FOUND"),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다.", "CHALLENGE_ERROR_404_COMMENT_NOT_FOUND"),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 댓글에 대한 권한이 없습니다.", "CHALLENGE_ERROR_403_COMMENT_FORBIDDEN"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
