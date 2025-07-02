package com.part4.team09.otboo.module.domain.file.exception;

import com.part4.team09.otboo.module.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum FileStorageErrorCode implements ErrorCode {

  LOCAL_STORAGE_FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패하였습니다."),
  LOCAL_FILE_STORAGE_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 폴더 생성에 실패하였습니다.");

  private final HttpStatus status;
  private final String message;

  FileStorageErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return this.status;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
