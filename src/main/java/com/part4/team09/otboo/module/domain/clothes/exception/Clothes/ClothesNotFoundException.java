package com.part4.team09.otboo.module.domain.clothes.exception.Clothes;

import com.part4.team09.otboo.module.domain.clothes.exception.ClothesErrorCode;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesException;
import java.util.UUID;

public class ClothesNotFoundException extends ClothesException {

  public ClothesNotFoundException() {
    super(ClothesErrorCode.CLOTHES_NOT_FOUND);
  }

  public static ClothesNotFoundException withId(UUID clothesId) {
    ClothesNotFoundException exception = new ClothesNotFoundException();
    exception.addDetail("clothesId", clothesId);
    return exception;
  }
}
