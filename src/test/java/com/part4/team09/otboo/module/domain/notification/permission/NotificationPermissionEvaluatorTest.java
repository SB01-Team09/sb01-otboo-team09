package com.part4.team09.otboo.module.domain.notification.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationPermissionEvaluatorTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationPermissionEvaluator notificationPermissionEvaluator;

  @Nested
  @DisplayName("알림 수신자 확인")
  public class IsNotificationReceiverTest {

    @Test
    @DisplayName("알림 수신자 확인 - true")
    void isNotificationReceiver_true() {
      // given
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      UUID notificationId = UUID.randomUUID();
      Notification mockNotification = mock(Notification.class);

      given(notificationRepository.findById(notificationId)).willReturn(Optional.of(mockNotification));
      given(mockNotification.getReceiverId()).willReturn(userId2);

      // when
      Boolean result = notificationPermissionEvaluator.isNotificationReceiver(userId1, notificationId);

      // then
      assertThat(result).isEqualTo(false);
      verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("알림 수신자 확인 - false : 알림 수신자가 아님")
    void isNotificationReceiver_false_whenUserIsNotReceiver() {
      // given
      UUID userId = UUID.randomUUID();
      UUID notificationId = UUID.randomUUID();
      Notification mockNotification = mock(Notification.class);

      given(notificationRepository.findById(notificationId)).willReturn(Optional.of(mockNotification));
      given(mockNotification.getReceiverId()).willReturn(userId);

      // when
      Boolean result = notificationPermissionEvaluator.isNotificationReceiver(userId, notificationId);

      // then
      assertThat(result).isEqualTo(true);
      verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("알림 수신자 확인 - false : 알림이 없음")
    void isFeedAuthor_false_whenFeedNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      UUID notificationId = UUID.randomUUID();

      given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

      // when
      Boolean result = notificationPermissionEvaluator.isNotificationReceiver(userId, notificationId);

      // then
      assertThat(result).isEqualTo(false);
      verify(notificationRepository).findById(notificationId);
    }
  }
}