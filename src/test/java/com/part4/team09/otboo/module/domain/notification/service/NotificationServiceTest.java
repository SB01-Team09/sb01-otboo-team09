package com.part4.team09.otboo.module.domain.notification.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.notification.dto.NotificationDto;
import com.part4.team09.otboo.module.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateFollowerRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateLocationRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import com.part4.team09.otboo.module.domain.notification.mapper.NotificationMapper;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationRepositoryQueryDSL notificationRepositoryQueryDSL;

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
      UUID notificationId = UUID.randomUUID();
      Notification mockNotification = mock(Notification.class);

      NotificationCreateRequest request = new NotificationCreateRequest(
          receiverId,
          "title",
          "content",
          Level.INFO
      );
      given(notificationRepository.save(any())).willReturn(mockNotification);
      given(mockNotification.getId()).willReturn(notificationId);

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

  @Test
  @DisplayName("알림 목록 조회 성공")
  void getNotificationListSuccess() {
    // given
    UUID receiverId = UUID.randomUUID();
    UUID idAfter = UUID.randomUUID();
    int limit = 5;
    LocalDateTime cursorTime = LocalDateTime.now().minusDays(1);
    String cursor = cursorTime.toString();

    // mock 알림 6개 (limit + 1)
    List<Notification> fakeNotifications = IntStream.range(0, 6)
        .mapToObj(i -> Notification.create(
            receiverId,
            "제목 " + i,
            "내용 " + i,
            Notification.Level.INFO
        ))
        .collect(Collectors.toList());

    // id, createdAt 설정
    for (int i = 0; i < fakeNotifications.size(); i++) {
      Notification noti = fakeNotifications.get(i);
      UUID id = UUID.randomUUID();
      LocalDateTime createdAt = cursorTime.plusMinutes(i);
      ReflectionTestUtils.setField(noti, "id", id);
      ReflectionTestUtils.setField(noti, "createdAt", createdAt);
    }

    // DTO mock
    List<NotificationDto> fakeDtos = fakeNotifications.stream()
        .map(noti -> new NotificationDto(
            (UUID) ReflectionTestUtils.getField(noti, "id"),
            noti.getCreatedAt(),
            noti.getReceiverId(),
            noti.getTitle(),
            noti.getContent(),
            noti.getLevel()
        ))
        .collect(Collectors.toList());

    // when
    when(notificationRepositoryQueryDSL.getNotifications(eq(receiverId), any(), any(),
        eq(limit + 1)))
        .thenReturn(fakeNotifications);
    when(notificationRepositoryQueryDSL.countNotifications(eq(receiverId))).thenReturn(123);

    for (int i = 0; i < fakeNotifications.size(); i++) {
      when(notificationMapper.toDto(fakeNotifications.get(i))).thenReturn(fakeDtos.get(i));
    }

    // when
    NotificationDtoCursorResponse result = notificationService.get(receiverId, cursor, idAfter,
        limit);

    // then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(limit); // limit만큼 잘린지 확인
    assertThat(result.hasNext()).isTrue();
    assertThat(result.totalCount()).isEqualTo(123);
    assertThat(result.sortBy()).isEqualTo("createdAt, id");
    assertThat(result.sortDirection()).isEqualTo(SortDirection.DESCENDING);

    NotificationDto lastDto = fakeDtos.get(limit - 1);
    assertThat(result.nextCursor()).isEqualTo(lastDto.createdAt().toString());
    assertThat(result.nextIdAfter()).isEqualTo(lastDto.id());

    // verify
    verify(notificationRepositoryQueryDSL).getNotifications(eq(receiverId), any(), any(),
        eq(limit + 1));
    verify(notificationRepositoryQueryDSL).countNotifications(eq(receiverId));
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