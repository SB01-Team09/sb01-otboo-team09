package com.part4.team09.otboo.module.domain.user.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends BaseException {

  public UserNotFoundException(UUID id) {
    super(UserErrorCode.USER_NOT_FOUND, Map.of("id", id));
  }
}
