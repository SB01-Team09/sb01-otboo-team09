package com.part4.team09.otboo.module.domain.file.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;
import java.util.Map;

public class FileStorageException extends BaseException {


  public FileStorageException(ErrorCode errorCode) {
    super(errorCode);
  }

  public FileStorageException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public FileStorageException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
