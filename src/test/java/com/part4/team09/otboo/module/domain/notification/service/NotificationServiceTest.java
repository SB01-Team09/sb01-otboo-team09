package com.part4.team09.otboo.module.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationService notificationService;

  @Nested
  @DisplayName("알림 생성")
  public class CreateNotificationTest {

    @Test
    @DisplayName("알림 생성 성공")
    void create_notification_success() {
      // given
      UUID receiverId = UUID.randomUUID();

      NotificationCreateRequest request = new NotificationCreateRequest(
          receiverId,
          "title",
          "content",
          Level.INFO
      );

      // when
      notificationService.create(request);

      // then
      verify(notificationRepository).save(any());
    }
  }
}