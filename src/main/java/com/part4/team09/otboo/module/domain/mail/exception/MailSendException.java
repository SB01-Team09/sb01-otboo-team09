package com.part4.team09.otboo.module.domain.mail.exception;

import static com.part4.team09.otboo.module.domain.mail.exception.MailErrorCode.UNKNOWN;

import com.part4.team09.otboo.module.common.exception.BaseException;
import com.part4.team09.otboo.module.common.exception.ErrorCode;

public class MailSendException extends BaseException {

  public MailSendException() {
    super(UNKNOWN);
  }

  public MailSendException(ErrorCode errorCode) {
    super(errorCode);
  }

  public static MailSendException withEmail(String email) {
    MailSendException exception = new MailSendException();
    exception.addDetail("email", email);
    return exception;
  }
}
