package com.cotato.itda.global.error.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode{

    // 404
    PROFILE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "삭제할 프로필 이미지가 존재하지 않습니다.", "PROFILE_ERROR_404_PROFILE_IMAGE_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
