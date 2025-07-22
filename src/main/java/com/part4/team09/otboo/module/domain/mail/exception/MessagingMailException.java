package com.part4.team09.otboo.module.domain.mail.exception;

import static com.part4.team09.otboo.module.domain.mail.exception.MailErrorCode.MESSAGING_ERROR;

public class MessagingMailException extends MailSendException {

  public MessagingMailException() {
    super(MESSAGING_ERROR);
  }

  public static MessagingMailException withEmail(String email) {
    MessagingMailException exception = new MessagingMailException();
    exception.addDetail("email", email);
    return exception;
  }
}
