package com.part4.team09.otboo.module.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public record ErrorResponse(

    String exceptionName,
    String message,
    Map<String, Object> details
) {
  public static ErrorResponse of(String exceptionName, String message) {
    return new ErrorResponse(exceptionName, message, null);
  }

  public static ErrorResponse of(String exceptionName, String message, Map<String, Object> details) {
    return new ErrorResponse(exceptionName, message, details);
  }
}
