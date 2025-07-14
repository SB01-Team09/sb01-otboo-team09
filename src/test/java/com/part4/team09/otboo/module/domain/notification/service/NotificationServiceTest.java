package com.part4.team09.otboo.module.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.List;
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

  @Mock
  private UserRepository userRepository;

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

  @Nested
  @DisplayName("모든 사용자에게 알림 생성")
  public class CreateAllNotificationTest {

    @Test
    @DisplayName("모든 사용자에게 알림 생성 성공")
    void create_all_notification_success() {
      // given
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      List<UUID> allUserIds = List.of(userId1, userId2);

      NotificationCreateAllRequest request = new NotificationCreateAllRequest(
          "title",
          "content",
          Level.INFO
      );

      given(userRepository.findAllIds()).willReturn(allUserIds);


      // when
      notificationService.createAll(request);

      // then
      verify(notificationRepository).saveAll(any());
    }
  }
}