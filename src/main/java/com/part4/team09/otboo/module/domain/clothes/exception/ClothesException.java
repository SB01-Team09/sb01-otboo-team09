package com.part4.team09.otboo.module.domain.clothes.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class ClothesException extends BaseException {

  public ClothesException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ClothesException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public ClothesException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
