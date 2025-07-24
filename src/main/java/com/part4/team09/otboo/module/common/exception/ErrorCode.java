package com.part4.team09.otboo.module.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

  HttpStatus getHttpStatus();

  String getMessage();
}
