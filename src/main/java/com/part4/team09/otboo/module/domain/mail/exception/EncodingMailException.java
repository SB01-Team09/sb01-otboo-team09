package com.part4.team09.otboo.module.domain.mail.exception;

import static com.part4.team09.otboo.module.domain.mail.exception.MailErrorCode.ENCODING_ERROR;

public class EncodingMailException extends MailSendException {

  public EncodingMailException() {
    super(ENCODING_ERROR);
  }

  public static EncodingMailException withEmail(String email) {
    EncodingMailException exception = new EncodingMailException();
    exception.addDetail("email", email);
    return exception;
  }
}
