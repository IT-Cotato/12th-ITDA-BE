package com.cotato.itda.global.error.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    // 500
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.", "IMAGE_ERROR_500_IMAGE_UPLOAD_FAILED"),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.", "IMAGE_ERROR_500_IMAGE_DELETE_FAILED"),

    // 400
    URL_NOT_VALID(HttpStatus.BAD_REQUEST, "잘못된 이미지 URL 입니다.", "IMAGE_ERROR_400_URL_NOT_VALID"),

    // 404
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 파일을 찾을 수 없습니다.", "IMAGE_ERROR_404_FILE_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
