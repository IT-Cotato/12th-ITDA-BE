package com.cotato.itda.domain.friendship.exception;

import com.cotato.itda.domain.friendship.exception.code.FriendshipErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import java.util.Map;

public class FriendshipException extends BusinessException {
  public FriendshipException(FriendshipErrorCode errorCode) {
    super(errorCode);
  }

  public FriendshipException(FriendshipErrorCode errorCode, Map<String, Object> reasons) {
    super(errorCode, reasons);
  }
}
