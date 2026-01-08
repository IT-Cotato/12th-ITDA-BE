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
    INVALID_S3_FOLDER(HttpStatus.BAD_REQUEST, "유효하지 않은 S3 폴더명입니다.", "IMAGE_ERROR_400_INVALID_S3_FOLDER"),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "파일명 형식이 올바르지 않습니다.", "IMAGE_ERROR_400_INVALID_FILE_NAME"),
    UNSUPPORTED_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 확장자입니다.", "IMAGE_ERROR_400_UNSUPPORTED_FILE_EXTENSION"),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "S3 버킷에 해당 파일이 존재하지 않습니다.", "IMAGE_ERROR_400_FILE_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
