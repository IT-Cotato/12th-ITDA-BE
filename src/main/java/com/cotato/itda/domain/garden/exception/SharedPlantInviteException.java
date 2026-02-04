package com.cotato.itda.domain.garden.exception;

import com.cotato.itda.domain.garden.exception.code.SharedPlantInviteErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import java.util.Map;

public class SharedPlantInviteException extends BusinessException {
    public SharedPlantInviteException(SharedPlantInviteErrorCode errorCode) {
        super(errorCode);
    }

    public SharedPlantInviteException(SharedPlantInviteErrorCode errorCode, Map<String, Object> reasons) {
        super(errorCode, reasons);
    }
}
