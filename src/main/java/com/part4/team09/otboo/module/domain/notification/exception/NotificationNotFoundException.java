package com.part4.team09.otboo.module.domain.notification.exception;

import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

  public NotificationNotFoundException() {
    super(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
  }

  public static NotificationNotFoundException withId(UUID id) {
    NotificationNotFoundException exception = new NotificationNotFoundException();
    exception.addDetail("id", id);
    return exception;
  }
}