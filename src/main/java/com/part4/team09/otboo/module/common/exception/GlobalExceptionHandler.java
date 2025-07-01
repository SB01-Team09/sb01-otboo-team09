package com.part4.team09.otboo.module.common.exception;

import com.part4.team09.otboo.module.common.dto.ErrorResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // validation
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex) {

    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
    FieldError fieldError = ex.getFieldErrors().get(0);

    log.info("Validation failed: {} - {}", fieldError.getField(),
      fieldError.getRejectedValue());

    ErrorResponse errorResponse = ErrorResponse.of(
      ex.getClass().getSimpleName(),
      fieldError.getDefaultMessage(),
      Map.of(fieldError.getField(), fieldError.getRejectedValue())
    );

    return createErrorResponseEntity(errorCode.getHttpStatus(), errorResponse);
  }

  // request method
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupport(
    HttpRequestMethodNotSupportedException ex) {

    log.info("Request method not supported: {}", ex.getMethod());

    CommonErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;

    ErrorResponse errorResponse = ErrorResponse.of(
      ex.getClass().getSimpleName(),
      ex.getMessage()
    );

    return createErrorResponseEntity(errorCode.getHttpStatus(), errorResponse);
  }

  // resource : 잘못된 uri
  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<ErrorResponse> handleNoResourceFoundException(
    NoResourceFoundException ex) {

    log.info("Request for unsupported URI: {}", ex.getResourcePath());

    CommonErrorCode errorCode = CommonErrorCode.NOT_FOUND;

    ErrorResponse errorResponse = ErrorResponse.of(
      ex.getClass().getSimpleName(),
      "지원하지 않는 URI입니다."
    );

    return createErrorResponseEntity(errorCode.getHttpStatus(), errorResponse);
  }

  // business
  @ExceptionHandler(BaseException.class)
  protected ResponseEntity<ErrorResponse> handleBusinessException(BaseException ex) {

    ErrorCode errorCode = ex.getErrorCode();

    ErrorResponse errorResponse = ErrorResponse.of(
      ex.getClass().getSimpleName(),
      ex.getMessage(),
      ex.getDetails()
    );

    return createErrorResponseEntity(errorCode.getHttpStatus(), errorResponse);
  }

  // other
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {

    log.error("Exception", ex);

    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

    ErrorResponse errorResponse = ErrorResponse.of(
      ex.getClass().getSimpleName(),
      errorCode.getMessage()
    );

    return createErrorResponseEntity(errorCode.getHttpStatus(), errorResponse);
  }

  private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status,
    ErrorResponse errorResponse) {
    return ResponseEntity
      .status(status)
      .body(errorResponse);
  }
}
