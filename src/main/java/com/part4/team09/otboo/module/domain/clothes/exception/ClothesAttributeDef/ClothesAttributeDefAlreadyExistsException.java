package com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef;

import com.part4.team09.otboo.module.domain.clothes.exception.ClothesErrorCode;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesException;

public class ClothesAttributeDefAlreadyExistsException extends ClothesException {

  public ClothesAttributeDefAlreadyExistsException() {
    super(ClothesErrorCode.DUPLICATE_ATTRIBUTE_DEF_NAME);
  }

  public static ClothesAttributeDefAlreadyExistsException withName(String name) {
    ClothesAttributeDefAlreadyExistsException exception = new ClothesAttributeDefAlreadyExistsException();
    exception.addDetail("name", name);
    return exception;
  }
}
