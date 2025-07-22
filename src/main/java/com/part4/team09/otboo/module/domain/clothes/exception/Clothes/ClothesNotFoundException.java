package com.part4.team09.otboo.module.domain.clothes.exception.Clothes;

import com.part4.team09.otboo.module.domain.clothes.exception.ClothesErrorCode;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesException;
import java.util.List;
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

  public static ClothesNotFoundException withIds(List<UUID> clothesIds) {
    ClothesNotFoundException exception = new ClothesNotFoundException();
    exception.addDetail("clothesIds", clothesIds);
    return exception;
  }

  public static ClothesNotFoundException withUrl(String url) {
    ClothesNotFoundException exception = new ClothesNotFoundException();
    exception.addDetail("url", url);
    return exception;
  }
}
