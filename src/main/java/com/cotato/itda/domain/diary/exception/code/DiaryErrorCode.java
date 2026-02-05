package com.cotato.itda.domain.diary.exception.code;

import com.cotato.itda.global.error.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiaryErrorCode implements ErrorCode {

    DIARY_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 날짜에 일기를 이미 작성했습니다.", "DIARY_ERROR_409_DIARY_ALREADY_EXISTS"),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 일기를 찾을 수 없습니다.", "DIARY_ERROR_404_DIARY_NOT_FOUND"),
    DIARY_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 일기에 대한 권한이 없습니다.", "DIARY_ERROR_403_DIARY_FORBIDDEN"),
    INVALID_EMOJI_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 이모지 코드값입니다.", "DIARY_ERROR_400_INVALID_EMOJI_CODE"),

    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 좋아요 누른 일기입니다.", "DIARY_ERROR_409_LIKE_ALREADY_EXISTS"),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "취소할 좋아요가 없습니다.", "DIARY_ERROR_404_LIKE_NOT_FOUND"),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다.", "DIARY_ERROR_404_COMMENT_NOT_FOUND"),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 댓글에 대한 권한이 없습니다.", "DIARY_ERROR_403_COMMENT_FORBIDDEN"),

    ;
    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
