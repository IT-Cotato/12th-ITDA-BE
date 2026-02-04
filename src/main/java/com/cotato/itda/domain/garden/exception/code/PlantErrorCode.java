package com.cotato.itda.domain.garden.exception.code;

import com.cotato.itda.global.error.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlantErrorCode implements ErrorCode {

        NOT_FOUND(
                        HttpStatus.NOT_FOUND,
                        "PLANT_404_NOT_FOUND",
                        "식물을 찾을 수 없습니다."),
        DUPLICATE_NAME(
                        HttpStatus.BAD_REQUEST,
                        "PLANT_400_DUPLICATE_NAME",
                        "이미 존재하는 식물 이름입니다.");

        private final HttpStatus httpStatus;
        private final String code;
        private final String message;
}
