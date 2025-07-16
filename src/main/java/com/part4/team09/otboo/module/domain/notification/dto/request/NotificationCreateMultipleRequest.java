package com.part4.team09.otboo.module.domain.notification.dto.request;

import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;

public record NotificationCreateMultipleRequest(
    String title,
    String content,
    Level level
) {

}
