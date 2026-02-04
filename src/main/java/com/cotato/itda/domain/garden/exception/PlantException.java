package com.cotato.itda.domain.garden.exception;

import com.cotato.itda.domain.garden.exception.code.PlantErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import java.util.Map;

public class PlantException extends BusinessException {
    public PlantException(PlantErrorCode errorCode) {
        super(errorCode);
    }

    public PlantException(PlantErrorCode errorCode, Map<String, Object> reasons) {super(errorCode, reasons);}
}
