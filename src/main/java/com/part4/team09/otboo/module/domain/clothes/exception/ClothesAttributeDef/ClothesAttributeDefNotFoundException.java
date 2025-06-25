package com.part4.team09.otboo.module.domain.clothes.exception.ClothesAttributeDef;

import com.part4.team09.otboo.module.domain.clothes.exception.ClothesErrorCode;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesException;
import java.util.UUID;

public class ClothesAttributeDefNotFoundException extends ClothesException {

  public ClothesAttributeDefNotFoundException() {
    super(ClothesErrorCode.ATTRIBUTE_DEF_NOT_FOUND);
  }

  public static ClothesAttributeDefNotFoundException withId(UUID id) {
    ClothesAttributeDefNotFoundException exception = new ClothesAttributeDefNotFoundException();
    exception.addDetail("id", id);
    return exception;
  }
}
