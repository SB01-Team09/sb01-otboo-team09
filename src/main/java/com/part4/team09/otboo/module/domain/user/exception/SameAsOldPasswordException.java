package com.part4.team09.otboo.module.domain.user.exception;

import java.util.UUID;

public class SameAsOldPasswordException extends UserException {

  public SameAsOldPasswordException() {
    super(UserErrorCode.SAME_AS_OLD_PASSWORD);
  }

  public static SameAsOldPasswordException withId(UUID id) {
    SameAsOldPasswordException exception = new SameAsOldPasswordException();
    exception.addDetail("id", id);
    return exception;
  }
}
