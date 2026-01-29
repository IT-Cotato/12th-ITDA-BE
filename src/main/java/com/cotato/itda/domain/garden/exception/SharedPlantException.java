package com.cotato.itda.domain.garden.exception;

import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import java.util.Map;

public class SharedPlantException extends BusinessException {
    public SharedPlantException(SharedPlantErrorCode errorCode) {
        super(errorCode);
    }

    public SharedPlantException(SharedPlantErrorCode errorCode, Map<String, Object> reasons) {
        super(errorCode, reasons);
    }
}
