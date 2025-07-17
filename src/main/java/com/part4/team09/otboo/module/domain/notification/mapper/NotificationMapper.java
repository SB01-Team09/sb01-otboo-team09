package com.part4.team09.otboo.module.domain.notification.mapper;

import com.part4.team09.otboo.module.domain.notification.dto.NotificationDto;
import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  NotificationDto toDto(Notification notification);
}
