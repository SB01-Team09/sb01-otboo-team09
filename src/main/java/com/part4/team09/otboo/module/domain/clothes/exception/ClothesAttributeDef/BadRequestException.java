package com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef;

import com.part4.team09.otboo.module.common.exception.CommonErrorCode;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesException;

public class BadRequestException extends ClothesException {

  public BadRequestException() {
    super(CommonErrorCode.INVALID_INPUT_VALUE);
  }

  public static BadRequestException withLimit(int limit) {
    BadRequestException exception = new BadRequestException();
    exception.addDetail("String", limit);
    return exception;
  }

  public static BadRequestException withSortBy(String sortBy) {
    BadRequestException exception = new BadRequestException();
    exception.addDetail("sortBy", sortBy);
    return exception;
  }
}
