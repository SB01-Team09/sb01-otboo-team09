package com.part4.team09.otboo.module.domain.recommendation.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum RecommendationErrorCode implements ErrorCode {

  LLM_RESPONSE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "LLM에서 올바른 응답을 받지 못해 파싱할 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;


  @Override
  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
