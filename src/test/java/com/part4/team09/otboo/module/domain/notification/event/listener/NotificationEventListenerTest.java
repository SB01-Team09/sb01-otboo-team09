package com.part4.team09.otboo.module.domain.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateFollowerRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateLocationRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import com.part4.team09.otboo.module.domain.notification.event.ClothesAttributeDefCreatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.ClothesAttributeDefUpdatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.DirectMessageReceivedEvent;
import com.part4.team09.otboo.module.domain.notification.event.FeedCommentedEvent;
import com.part4.team09.otboo.module.domain.notification.event.FeedCreatedFollowerEvent;
import com.part4.team09.otboo.module.domain.notification.event.FeedLikedEvent;
import com.part4.team09.otboo.module.domain.notification.event.FollowedEvent;
import com.part4.team09.otboo.module.domain.notification.event.RapidTemperatureDropEvent;
import com.part4.team09.otboo.module.domain.notification.event.RapidTemperatureRiseEvent;
import com.part4.team09.otboo.module.domain.notification.event.RoleChangedEvent;
import com.part4.team09.otboo.module.domain.notification.event.WeatherNotificationCreateEvent;
import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock
  NotificationService notificationService;

  @Mock
  ObjectMapper objectMapper;

  @InjectMocks
  NotificationEventListener notificationEventListener;

  @Nested
  @DisplayName("RoleChangedEvent 수신 시")
  class HandleRoleChangedEvent {

    final UUID receiverId = UUID.randomUUID();
    final RoleChangedEvent event = new RoleChangedEvent(receiverId, Role.USER, Role.ADMIN);
    final String payload = String.format(
        "{\"receiverId\":\"%s\",\"previousRole\":\"USER\",\"newRole\":\"ADMIN\"}", receiverId
    );

    @Test
    @DisplayName("handleRoleChangedEvent 성공")
    void handleRoleChangedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, RoleChangedEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleRoleChangedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateRequest.class);
      verify(notificationService).create(captor.capture());

      NotificationCreateRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("내 권한이 변경되었어요.");
      assertThat(actual.content())
          .isEqualTo("내 권한이 [USER]에서 [ADMIN](으)로 변경되었어요.");
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleRoleChangedEvent 실패 - 역직렬화 실패")
    void handleRoleChangedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, RoleChangedEvent.class)).willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleRoleChangedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleRoleChangedEvent 실패 - 서비스 내부 예외")
    void handleRoleChangedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, RoleChangedEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 장애")).given(notificationService).create(any());

      // when
      notificationEventListener.handleRoleChangedEvent(payload);

      // then
      then(notificationService).should().create(any());
    }
  }

  @Nested
  @DisplayName("ClothesAttributeDefCreatedEvent 수신 시")
  class HandleClothesAttributeDefCreatedEvent {

    final ClothesAttributeDefCreatedEvent event = new ClothesAttributeDefCreatedEvent("소재");
    final String payload = "{\"name\":\"소재\"}";

    @Test
    @DisplayName("handleClothesAttributeDefCreatedEvent 성공")
    void handleClothesAttributeDefCreatedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, ClothesAttributeDefCreatedEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleClothesAttributeDefCreatedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateAllRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateAllRequest.class);
      verify(notificationService).createAll(captor.capture());

      NotificationCreateAllRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("새로운 의상 속성이 추가되었어요.");
      assertThat(actual.content()).isEqualTo("내 의상에 [소재] 속성을 추가해보세요.");
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleClothesAttributeDefCreatedEvent 실패 - 역직렬화 실패")
    void handleClothesAttributeDefCreatedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, ClothesAttributeDefCreatedEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleClothesAttributeDefCreatedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleClothesAttributeDefCreatedEvent 실패 - 서비스 내부 예외")
    void handleClothesAttributeDefCreatedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, ClothesAttributeDefCreatedEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).createAll(any());

      // when
      notificationEventListener.handleClothesAttributeDefCreatedEvent(payload);

      // then
      then(notificationService).should().createAll(any());
    }
  }

  @Nested
  @DisplayName("ClothesAttributeDefUpdatedEvent 수신 시")
  class HandleClothesAttributeDefUpdatedEvent {

    final ClothesAttributeDefUpdatedEvent event = new ClothesAttributeDefUpdatedEvent("소재");
    final String payload = "{\"name\":\"패딩\"}";

    @Test
    @DisplayName("handleClothesAttributeDefUpdatedEvent 성공")
    void handleClothesAttributeDefUpdatedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, ClothesAttributeDefUpdatedEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleClothesAttributeDefUpdatedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateAllRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateAllRequest.class);
      verify(notificationService).createAll(captor.capture());

      NotificationCreateAllRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("의상 속성이 변경되었어요.");
      assertThat(actual.content()).isEqualTo("[소재] 속성을 확인해보세요.");
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleClothesAttributeDefUpdatedEvent 실패 - 역직렬화 실패")
    void handleClothesAttributeDefUpdatedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, ClothesAttributeDefUpdatedEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleClothesAttributeDefUpdatedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleClothesAttributeDefUpdatedEvent 실패 - 서비스 내부 예외")
    void handleClothesAttributeDefUpdatedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, ClothesAttributeDefUpdatedEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).createAll(any());

      // when
      notificationEventListener.handleClothesAttributeDefUpdatedEvent(payload);

      // then
      then(notificationService).should().createAll(any());
    }
  }

  @Nested
  @DisplayName("FeedLikedEvent 수신 시")
  class HandleFeedLikedEvent {

    final UUID receiverId = UUID.randomUUID();
    final FeedLikedEvent event = new FeedLikedEvent(receiverId, "username", "feedContent");
    final String payload = String.format(
        "{\"receiverId\":\"%s\",\"username\":\"%s\",\"feedContent\":\"%s\"}", receiverId, "username", "feedContent"
    );

    @Test
    @DisplayName("handleFeedLikedEvent 성공")
    void handleFeedLikedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedLikedEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleFeedLikedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateRequest.class);
      verify(notificationService).create(captor.capture());

      NotificationCreateRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("username님이 내 피드를 좋아합니다.");
      assertThat(actual.content()).isEqualTo("feedContent");
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleFeedLikedEvent 실패 - 역직렬화 실패")
    void handleFeedLikedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedLikedEvent.class)).willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleFeedLikedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleFeedLikedEvent 실패 - 서비스 내부 예외")
    void handleFeedLikedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedLikedEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).create(any());

      // when
      notificationEventListener.handleFeedLikedEvent(payload);

      // then
      then(notificationService).should().create(any());
    }
  }

  @Nested
  @DisplayName("FeedCommentedEvent 수신 시")
  class HandleFeedCommentedEvent {

    final UUID receiverId = UUID.randomUUID();
    final FeedCommentedEvent event = new FeedCommentedEvent(receiverId, "username", "content");
    final String payload = String.format(
        "{\"receiverId\":\"%s\",\"username\":\"%s\",\"content\":\"%s\"}", receiverId, "username", "content"
    );

    @Test
    @DisplayName("handleFeedCommentedEvent 성공")
    void handleFeedCommentedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedCommentedEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleFeedCommentedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateRequest.class);
      verify(notificationService).create(captor.capture());

      NotificationCreateRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("username님이 댓글을 달았어요.");
      assertThat(actual.content()).isEqualTo("content");
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleFeedCommentedEvent 실패 - 역직렬화 실패")
    void handleFeedCommentedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedCommentedEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleFeedCommentedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleFeedCommentedEvent 실패 - 서비스 내부 예외")
    void handleFeedCommentedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedCommentedEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).create(any());

      // when
      notificationEventListener.handleFeedCommentedEvent(payload);

      // then
      then(notificationService).should().create(any());
    }
  }

  @Nested
  @DisplayName("FeedCreatedFollowerEvent 수신 시")
  class HandleFeedCreatedFollowerEvent {

    final UUID authorId = UUID.randomUUID();
    final FeedCreatedFollowerEvent event = new FeedCreatedFollowerEvent(authorId, "authorName", "content");
    final String payload = String.format(
        "{\"authorId\":\"%s\",\"authorName\":\"%s\",\"content\":\"%s\"}", authorId, "authorName", "content"
    );

    @Test
    @DisplayName("handleFeedCreatedEvent 성공")
    void handleFeedCreatedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedCreatedFollowerEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleFeedCreatedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateFollowerRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateFollowerRequest.class);
      verify(notificationService).createFollower(captor.capture());

      NotificationCreateFollowerRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("authorName님이 새로운 피드를 작성했어요.");
      assertThat(actual.content()).isEqualTo("content");
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleFeedCreatedEvent 실패 - 역직렬화 실패")
    void handleFeedCreatedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedCreatedFollowerEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleFeedCreatedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleFeedCreatedEvent 실패 - 서비스 내부 예외")
    void handleFeedCreatedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FeedCreatedFollowerEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).createFollower(any());

      // when
      notificationEventListener.handleFeedCreatedEvent(payload);

      // then
      then(notificationService).should().createFollower(any());
    }
  }

  @Nested
  @DisplayName("FollowedEvent 수신 시")
  class HandleFollowedEvent {

    final UUID receiverId = UUID.randomUUID();
    final FollowedEvent event = new FollowedEvent(receiverId, "followerName");
    final String payload = String.format(
        "{\"receiverId\":\"%s\",\"followerName\":\"%s\"}", receiverId, "followerName"
    );

    @Test
    @DisplayName("handleFollowedEvent 성공")
    void handleFollowedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, FollowedEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleFollowedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateRequest.class);
      verify(notificationService).create(captor.capture());

      NotificationCreateRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("followerName님이 나를 팔로우했어요.");
      assertThat(actual.content()).isEmpty();
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleFollowedEvent 실패 - 역직렬화 실패")
    void handleFollowedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FollowedEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleFollowedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleFollowedEvent 실패 - 서비스 내부 예외")
    void handleFollowedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, FollowedEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).create(any());

      // when
      notificationEventListener.handleFollowedEvent(payload);

      // then
      then(notificationService).should().create(any());
    }
  }

  @Nested
  @DisplayName("DirectMessageReceivedEvent 수신 시")
  class HandleDirectMessageReceivedEvent {

    final UUID receiverId = UUID.randomUUID();
    final DirectMessageReceivedEvent event = new DirectMessageReceivedEvent(receiverId, "senderName", "안녕하세요");
    final String payload = String.format(
        "{\"receiverId\":\"%s\",\"senderName\":\"%s\",\"content\":\"%s\"}", receiverId, "senderName", "안녕하세요"
    );

    @Test
    @DisplayName("handleDirectMessageReceivedEvent 성공")
    void handleDirectMessageReceivedEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, DirectMessageReceivedEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleDirectMessageReceivedEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateRequest.class);
      verify(notificationService).create(captor.capture());

      NotificationCreateRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("[DM] senderName");
      assertThat(actual.content()).isEqualTo("안녕하세요");
      assertThat(actual.level()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("handleDirectMessageReceivedEvent 실패 - 역직렬화 실패")
    void handleDirectMessageReceivedEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, DirectMessageReceivedEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleDirectMessageReceivedEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleDirectMessageReceivedEvent 실패 - 서비스 내부 예외")
    void handleDirectMessageReceivedEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, DirectMessageReceivedEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).create(any());

      // when
      notificationEventListener.handleDirectMessageReceivedEvent(payload);

      // then
      then(notificationService).should().create(any());
    }
  }

  @Nested
  @DisplayName("RapidTemperatureRiseEvent 수신 시")
  class HandleRapidTemperatureRiseEvent {

    final String locationId = "1111051500";
    final RapidTemperatureRiseEvent event = new RapidTemperatureRiseEvent(locationId);
    final String payload = String.format("{\"locationId\":\"%s\"}", locationId);

    @Test
    @DisplayName("handleRapidTemperatureRiseEvent 성공")
    void handleRapidTemperatureRiseEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, RapidTemperatureRiseEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleRapidTemperatureRiseEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateLocationRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateLocationRequest.class);
      verify(notificationService).createLocation(captor.capture());

      NotificationCreateLocationRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("어제보다 기온이 급격히 높아졌어요.");
      assertThat(actual.content()).isEqualTo("외출 시 옷차림에 유의하세요.");
      assertThat(actual.level()).isEqualTo(Level.WARNING);
      assertThat(actual.locationId()).isEqualTo(locationId);
    }

    @Test
    @DisplayName("handleRapidTemperatureRiseEvent 실패 - 역직렬화 실패")
    void handleRapidTemperatureRiseEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, RapidTemperatureRiseEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleRapidTemperatureRiseEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleRapidTemperatureRiseEvent 실패 - 서비스 내부 예외")
    void handleRapidTemperatureRiseEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, RapidTemperatureRiseEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).createLocation(any());

      // when
      notificationEventListener.handleRapidTemperatureRiseEvent(payload);

      // then
      then(notificationService).should().createLocation(any());
    }
  }

  @Nested
  @DisplayName("RapidTemperatureDropEvent 수신 시")
  class HandleRapidTemperatureDropEvent {

    final String locationId = "1111051500";
    final RapidTemperatureDropEvent event = new RapidTemperatureDropEvent(locationId);
    final String payload = String.format("{\"locationId\":\"%s\"}", locationId);

    @Test
    @DisplayName("handleRapidTemperatureDropEvent 성공")
    void handleRapidTemperatureDropEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, RapidTemperatureDropEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleRapidTemperatureDropEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateLocationRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateLocationRequest.class);
      verify(notificationService).createLocation(captor.capture());

      NotificationCreateLocationRequest actual = captor.getValue();
      assertThat(actual.title()).isEqualTo("어제보다 기온이 급격히 낮아졌어요.");
      assertThat(actual.content()).isEqualTo("외출 시 옷차림에 유의하세요.");
      assertThat(actual.level()).isEqualTo(Level.WARNING);
      assertThat(actual.locationId()).isEqualTo(locationId);
    }

    @Test
    @DisplayName("handleRapidTemperatureDropEvent 실패 - 역직렬화 실패")
    void handleRapidTemperatureDropEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, RapidTemperatureDropEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleRapidTemperatureDropEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleRapidTemperatureDropEvent 실패 - 서비스 내부 예외")
    void handleRapidTemperatureDropEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, RapidTemperatureDropEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).createLocation(any());

      // when
      notificationEventListener.handleRapidTemperatureDropEvent(payload);

      // then
      then(notificationService).should().createLocation(any());
    }
  }

  @Nested
  @DisplayName("WeatherNotificationCreateEvent 수신 시")
  class HandleWeatherNotificationCreateEvent {

    final String locationId = "1111051500";
    final WeatherNotificationCreateEvent event = new WeatherNotificationCreateEvent(
            locationId, "오늘은 비가(이) 올 예정입니다.", "외출 시 우산을 챙기세요."
        );
    final String payload = String.format(
        "{\"locationId\":\"%s\",\"title\":\"오늘은 비가(이) 올 예정입니다.\",\"content\":\"외출 시 우산을 챙기세요.\"}",
        locationId);

    @Test
    @DisplayName("handleWeatherNotificationCreateEvent 성공")
    void handleWeatherNotificationCreateEvent_success() throws Exception {
      // given
      given(objectMapper.readValue(payload, WeatherNotificationCreateEvent.class)).willReturn(event);

      // when
      notificationEventListener.handleWeatherNotificationCreateEvent(payload);

      // then
      ArgumentCaptor<NotificationCreateLocationRequest> captor =
          ArgumentCaptor.forClass(NotificationCreateLocationRequest.class);
      verify(notificationService).createLocation(captor.capture());

      NotificationCreateLocationRequest actual = captor.getValue();
      assertThat(actual.locationId()).isEqualTo(locationId);
      assertThat(actual.title()).isEqualTo("오늘은 비가(이) 올 예정입니다.");
      assertThat(actual.content()).isEqualTo("외출 시 우산을 챙기세요.");
      assertThat(actual.level()).isEqualTo(Level.WARNING);
    }

    @Test
    @DisplayName("handleWeatherNotificationCreateEvent 실패 - 역직렬화 실패")
    void handleWeatherNotificationCreateEvent_throwsJsonProcessingException() throws Exception {
      // given
      given(objectMapper.readValue(payload, WeatherNotificationCreateEvent.class))
          .willThrow(JsonProcessingException.class);

      // when
      notificationEventListener.handleWeatherNotificationCreateEvent(payload);

      // then
      verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("handleWeatherNotificationCreateEvent 실패 - 서비스 내부 예외")
    void handleWeatherNotificationCreateEvent_throwsRuntimeException() throws Exception {
      // given
      given(objectMapper.readValue(payload, WeatherNotificationCreateEvent.class)).willReturn(event);
      willThrow(new RuntimeException("DB 오류")).given(notificationService).createLocation(any());

      // when
      notificationEventListener.handleWeatherNotificationCreateEvent(payload);

      // then
      then(notificationService).should().createLocation(any());
    }
  }
}
