package com.part4.team09.otboo.module.domain.notification.event.Listener;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  // 권한 변경
  @KafkaListener(
      topics = "otboo.role_change",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleRoleChangedEvent(String kafkaEvent) {
    try {
      RoleChangedEvent event = objectMapper.readValue(kafkaEvent, RoleChangedEvent.class);

      String title = "내 권한이 변경되었어요.";
      String content = String.format("내 권한이 [%s]에서 [%s](으)로 변경되었어요.",
          event.previousRole(), event.newRole());

      NotificationCreateRequest request = new NotificationCreateRequest(
          event.receiverId(),
          title,
          content,
          Level.INFO
      );

      notificationService.create(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: RoleChangedEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: RoleChangedEvent - payload={}", kafkaEvent, e);
    }
  }

  // 의상 속성 추가
  @KafkaListener(
      topics = "otboo.clothes_attribute_def_create",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleClothesAttributeDefCreatedEvent(String kafkaEvent) {
    try {
      ClothesAttributeDefCreatedEvent event = objectMapper.readValue(kafkaEvent, ClothesAttributeDefCreatedEvent.class);

      String title = "새로운 의상 속성이 추가되었어요.";
      String content = String.format("내 의상에 [%s] 속성을 추가해보세요.", event.name());

      NotificationCreateAllRequest request = new NotificationCreateAllRequest(
          title,
          content,
          Level.INFO
      );

      notificationService.createAll(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: ClothesAttributeDefCreatedEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: ClothesAttributeDefCreatedEvent - payload={}", kafkaEvent, e);
    }
  }

  // 의상 속성 변경
  @KafkaListener(
      topics = "otboo.clothes_attribute_def_update",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleClothesAttributeDefUpdatedEvent(String kafkaEvent) {
    try {
      ClothesAttributeDefUpdatedEvent event = objectMapper.readValue(kafkaEvent, ClothesAttributeDefUpdatedEvent.class);

      String title = "의상 속성이 변경되었어요.";
      String content = String.format("[%s] 속성을 확인해보세요.", event.name());

      NotificationCreateAllRequest request = new NotificationCreateAllRequest(
          title,
          content,
          Level.INFO
      );

      notificationService.createAll(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: ClothesAttributeDefUpdatedEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: ClothesAttributeDefUpdatedEvent - payload={}", kafkaEvent, e);
    }
  }

  // 내 피드에 좋아요
  @KafkaListener(
      topics = "otboo.feed_like",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleFeedLikedEvent(String kafkaEvent) {
    try {
      FeedLikedEvent event = objectMapper.readValue(kafkaEvent, FeedLikedEvent.class);

      String title = String.format("%s님이 내 피드를 좋아합니다.", event.username());
      String content = event.feedContent();

      NotificationCreateRequest request = new NotificationCreateRequest(
          event.receiverId(),
          title,
          content,
          Level.INFO
      );

      notificationService.create(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: FeedLikedEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: FeedLikedEvent - payload={}", kafkaEvent, e);
    }
  }

  // 내 피드에 댓글 등록
  @KafkaListener(
      topics = "otboo.feed_comment",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleFeedCommentedEvent(String kafkaEvent) {
    try {
      FeedCommentedEvent event = objectMapper.readValue(kafkaEvent, FeedCommentedEvent.class);

      String title = String.format("%s님이 댓글을 달았어요.", event.username());
      String content = event.content();

      NotificationCreateRequest request = new NotificationCreateRequest(
          event.receiverId(),
          title,
          content,
          Level.INFO
      );

      notificationService.create(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: FeedCommentedEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: FeedCommentedEvent - payload={}", kafkaEvent, e);
    }
  }

  // 팔로우한 사용자가 피드 등록
  @KafkaListener(
      topics = "otboo.feed_create_follower",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleFeedCreatedEvent(String kafkaEvent) {
    try {
      FeedCreatedFollowerEvent event = objectMapper.readValue(kafkaEvent, FeedCreatedFollowerEvent.class);

      String title = String.format("%s님이 새로운 피드를 작성했어요.", event.authorName());
      String content = event.content();

      NotificationCreateFollowerRequest request = new NotificationCreateFollowerRequest(
          event.authorId(),
          title,
          content,
          Level.INFO
      );

      notificationService.createFollower(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: FeedCreatedFollowerEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: FeedCreatedFollowerEvent - payload={}", kafkaEvent, e);
    }
  }

  // 다른 사용자가 나를 팔로우
  @KafkaListener(
      topics = "otboo.follow",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleFollowedEvent(String kafkaEvent) {
    try {
      FollowedEvent event = objectMapper.readValue(kafkaEvent, FollowedEvent.class);

      String title = String.format("%s님이 나를 팔로우했어요.", event.followerName());
      String content = "";

      NotificationCreateRequest request = new NotificationCreateRequest(
          event.receiverId(),
          title,
          content,
          Level.INFO
      );

      notificationService.create(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: FollowedEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: FollowedEvent - payload={}", kafkaEvent, e);
    }
  }

  // DM 수신
  @KafkaListener(
      topics = "otboo.direct_message_receive",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleDirectMessageReceivedEvent(String kafkaEvent) {
    try {
      DirectMessageReceivedEvent event = objectMapper.readValue(kafkaEvent, DirectMessageReceivedEvent.class);

      String title = String.format("[DM] %s", event.senderName());
      String content = event.content();

      NotificationCreateRequest request = new NotificationCreateRequest(
          event.receiverId(),
          title,
          content,
          Level.INFO
      );

      notificationService.create(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: DirectMessageReceivedEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: DirectMessageReceivedEvent - payload={}", kafkaEvent, e);
    }
  }

  // 급격한 기온 상승 예정
  @KafkaListener(
      topics = "otboo.rapid_temperature_rise",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleRapidTemperatureRiseEvent(String kafkaEvent) {
    try {
      RapidTemperatureRiseEvent event = objectMapper.readValue(kafkaEvent, RapidTemperatureRiseEvent.class);

      String title = "어제보다 기온이 급격히 높아졌어요.";
      String content = "외출 시 옷차림에 유의하세요.";

      NotificationCreateLocationRequest request = new NotificationCreateLocationRequest(
          event.locationId(),
          title,
          content,
          Level.WARNING
      );

      notificationService.createLocation(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: RapidTemperatureRiseEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: RapidTemperatureRiseEvent - payload={}", kafkaEvent, e);
    }
  }

  // 급격한 기온 하강 예정
  @KafkaListener(
      topics = "otboo.rapid_temperature_drop",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleRapidTemperatureDropEvent(String kafkaEvent) {
    try {
      RapidTemperatureDropEvent event = objectMapper.readValue(kafkaEvent, RapidTemperatureDropEvent.class);

      String title = "어제보다 기온이 급격히 낮아졌어요.";
      String content = "외출 시 옷차림에 유의하세요.";

      NotificationCreateLocationRequest request = new NotificationCreateLocationRequest(
          event.locationId(),
          title,
          content,
          Level.WARNING
      );

      notificationService.createLocation(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: RapidTemperatureDropEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: RapidTemperatureDropEvent - payload={}", kafkaEvent, e);
    }
  }

  // 비, 눈, 소나기 등 예정
  @KafkaListener(
      topics = "otboo.weather_notification_create",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void handleWeatherNotificationCreateEvent(String kafkaEvent) {
    try {
      WeatherNotificationCreateEvent event = objectMapper.readValue(kafkaEvent, WeatherNotificationCreateEvent.class);

      NotificationCreateLocationRequest request = new NotificationCreateLocationRequest(
          event.locationId(),
          event.title(),
          event.content(),
          Level.WARNING
      );

      notificationService.createLocation(request);
    } catch (JsonProcessingException e) {
      log.error("Kafka 역직렬화 실패: WeatherNotificationCreateEvent - payload={}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("Kafka 처리 실패: WeatherNotificationCreateEvent - payload={}", kafkaEvent, e);
    }
  }
}
