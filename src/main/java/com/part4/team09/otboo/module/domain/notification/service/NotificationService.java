package com.part4.team09.otboo.module.domain.notification.service;

import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public void create(NotificationCreateRequest notificationCreateRequest) {
    Notification notification = Notification.create(
        notificationCreateRequest.receiverId(),
        notificationCreateRequest.title(),
        notificationCreateRequest.content(),
        notificationCreateRequest.level()
    );

    notificationRepository.save(notification);
  }

}
