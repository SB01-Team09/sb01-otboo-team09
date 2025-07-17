package com.part4.team09.otboo.module.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateFollowerRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateLocationRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import com.part4.team09.otboo.module.domain.notification.mapper.NotificationMapper;
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
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @Mock
  private UserRepository userRepository;

  @Mock
  private FollowRepository followRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

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

  @Nested
  @DisplayName("팔로워에게 알림 생성")
  public class CreateFollowerNotificationTest {

    @Test
    @DisplayName("팔로워에게 알림 생성 성공")
    void create_follower_notification_success() {
      // given
      UUID authorId = UUID.randomUUID();
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      List<UUID> followerIds = List.of(userId1, userId2);

      NotificationCreateFollowerRequest request = new NotificationCreateFollowerRequest(
        authorId,
        "title",
        "content",
        Level.INFO
      );

      given(followRepository.findFollowerIdsByFolloweeId(authorId)).willReturn(followerIds);

      // when
      notificationService.createFollower(request);

      // then
      verify(notificationRepository).saveAll(any());
    }
  }

  @Nested
  @DisplayName("특정 지역에 알림 생성")
  public class CreateLocationNotificationTest {

    @Test
    @DisplayName("특정 지역에 알림 생성 성공")
    void create_location_notification_success() {
      // given
      String locationId = "1111051500";
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      List<UUID> userIdsInLocation = List.of(userId1, userId2);

      NotificationCreateLocationRequest request = new NotificationCreateLocationRequest(
        locationId,
        "title",
        "content",
        Level.WARNING
      );

      given(userRepository.findUserIdsByLocationId(locationId)).willReturn(userIdsInLocation);

      // when
      notificationService.createLocation(request);

      //then
      verify(notificationRepository).saveAll(any());
    }
  }

  @Nested
  @DisplayName("알림 삭제")
  public class DeleteNotificationTest {

    @Test
    @DisplayName("알림 삭제 성공")
    void delete_notification_success() {
      // given
      UUID notificationId = UUID.randomUUID();

      given(notificationRepository.existsById(notificationId)).willReturn(true);

      // when
      notificationService.delete(notificationId);

      // then
      verify(notificationRepository).deleteById(notificationId);

    }
  }
}