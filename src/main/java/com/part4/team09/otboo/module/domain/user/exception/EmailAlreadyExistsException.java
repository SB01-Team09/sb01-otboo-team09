package com.part4.team09.otboo.module.domain.user.exception;

import com.part4.team09.otboo.module.common.exception.BaseException;
import java.util.Map;

public class EmailAlreadyExistsException extends BaseException {

  public EmailAlreadyExistsException(String email) {
    super(UserErrorCode.EMAIL_ALREADY_EXISTS, Map.of("email", email));
  }

}
