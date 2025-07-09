package com.part4.team09.otboo.module.domain.clothes.exception.SelectableValue;

import com.part4.team09.otboo.module.domain.clothes.exception.ClothesErrorCode;
import com.part4.team09.otboo.module.domain.clothes.exception.ClothesException;
import java.util.UUID;

public class SelectableValueNotFoundException extends ClothesException {

  public SelectableValueNotFoundException() {
    super(ClothesErrorCode.SELECTABLE_VALUE_NOT_FOUND);
  }

  public static SelectableValueNotFoundException withItem(String item) {
    SelectableValueNotFoundException exception = new SelectableValueNotFoundException();
    exception.addDetail("item", item);
    return exception;
  }

  public static SelectableValueNotFoundException withId(UUID selectableValueId) {
    SelectableValueNotFoundException exception = new SelectableValueNotFoundException();
    exception.addDetail("selectableValueId", selectableValueId);
    return exception;
  }
}
