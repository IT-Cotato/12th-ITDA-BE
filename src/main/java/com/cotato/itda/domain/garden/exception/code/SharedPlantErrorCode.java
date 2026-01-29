package com.cotato.itda.domain.garden.exception.code;

import com.cotato.itda.global.error.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SharedPlantErrorCode implements ErrorCode {

    ALREADY_HAS_SHARED_PLANT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_ALREADY_HAS_SHARED_PLANT",
            "이미 키우고 있는 식물이 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
