package com.part4.team09.otboo.module.domain.user.exception;

import java.util.UUID;

public class UserNotFoundException extends UserException {

  public UserNotFoundException() {
    super(UserErrorCode.USER_NOT_FOUND);
  }

  public static UserNotFoundException withId(UUID id) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("id", id);
    return exception;
  }
}
