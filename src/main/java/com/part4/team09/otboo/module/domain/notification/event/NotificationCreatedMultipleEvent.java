package com.part4.team09.otboo.module.domain.notification.event;

import com.part4.team09.otboo.module.domain.notification.dto.NotificationDto;
import java.util.List;

public record NotificationCreatedMultipleEvent(
    List<NotificationDto> notificationDtos
) {

}
