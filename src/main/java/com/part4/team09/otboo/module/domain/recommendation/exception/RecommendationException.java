package com.part4.team09.otboo.module.domain.recommendation.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class RecommendationException extends BaseException {

  public RecommendationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public RecommendationException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public RecommendationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

}
