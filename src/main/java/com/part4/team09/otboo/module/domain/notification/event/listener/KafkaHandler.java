package com.part4.team09.otboo.module.domain.notification.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHandler {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  // 권한 변경
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRoleChangedEvent(RoleChangedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.role_change", event.receiverId().toString(), payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: RoleChangedEvent for receiverId={}, previousRole={}, newRole={}",
          event.receiverId(), event.previousRole(), event.newRole(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: RoleChangedEvent for receiverId={}, previousRole={}, newRole={}",
          event.receiverId(), event.previousRole(), event.newRole(), e);
    }
  }

  // 의상 속성 추가
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleClothesAttributeDefCreatedEvent(ClothesAttributeDefCreatedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.clothes_attribute_def_create", payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: ClothesAttributeDefCreatedEvent for name={}",
          event.name(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: ClothesAttributeDefCreatedEvent for name={}",
          event.name(), e);
    }
  }

  // 의상 속성 변경
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleClothesAttributeDefUpdatedEvent(ClothesAttributeDefUpdatedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.clothes_attribute_def_update", payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: ClothesAttributeDefUpdatedEvent for name={}",
          event.name(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: ClothesAttributeDefUpdatedEvent for name={}",
          event.name(), e);
    }
  }

  // 내 피드에 좋아요
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedLikedEvent(FeedLikedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.feed_like", event.receiverId().toString(), payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: FeedLikedEvent for receiverId={}, username={}, feedContent={}",
          event.receiverId(), event.username(), event.feedContent(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: FeedLikedEvent for receiverId={}, username={}, feedContent={}",
          event.receiverId(), event.username(), event.feedContent(), e);
    }
  }

  // 내 피드에 댓글 등록
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedCommentedEvent(FeedCommentedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.feed_comment", event.receiverId().toString(), payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: FeedCommentedEvent for receiverId={}, username={}, content={}",
          event.receiverId(), event.username(), event.content(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: FeedCommentedEvent for receiverId={}, username={}, content={}",
          event.receiverId(), event.username(), event.content(), e);
    }
  }

  // 팔로우한 사용자가 피드 등록
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedCreatedFollowerEvent(FeedCreatedFollowerEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.feed_create_follower", payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: FeedCreatedFollowerEvent for authorId={}, authorName={}, content={}",
          event.authorId(), event.authorName(), event.content(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: FeedCreatedFollowerEvent for authorId={}, authorName={}, content={}",
          event.authorId(), event.authorName(), event.content(), e);
    }
  }

  // 다른 사용자가 나를 팔로우
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFollowedEvent(FollowedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.follow", payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: FollowedEvent for receiverId={}, followerName={}",
          event.receiverId(), event.followerName(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: FollowedEvent for receiverId={}, followerName={}",
          event.receiverId(), event.followerName(), e);
    }
  }

  // DM 수신
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDirectMessageReceivedEvent(DirectMessageReceivedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.direct_message_receive", event.receiverId().toString(), payload);
    } catch (JsonProcessingException e) {
      log.error(
          "Kafka 직렬화 실패: DirectMessageReceivedEvent for receiverId={}, senderName={}, content={}",
          event.receiverId(), event.senderName(), event.content(), e);
    } catch (Exception e) {
      log.error(
          "Kafka 전송 실패: DirectMessageReceivedEvent for receiverId={}, senderName={}, content={}",
          event.receiverId(), event.senderName(), event.content(), e);
    }
  }

  // 급격한 기온 상승 예정
  @Async
  @EventListener
  public void handleRapidTemperatureRiseEvent(RapidTemperatureRiseEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.rapid_temperature_rise", payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: RapidTemperatureRiseEvent for locationId={}",
          event.locationId(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: RapidTemperatureRiseEvent for locationId={}",
          event.locationId(), e);
    }
  }

  // 급격한 기온 하강 예정
  @Async
  @EventListener
  public void handleRapidTemperatureDropEvent(RapidTemperatureDropEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.rapid_temperature_drop", payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: RapidTemperatureDropEvent for locationId={}",
          event.locationId(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: RapidTemperatureRiseEvent for locationId={}",
          event.locationId(), e);
    }
  }

  // 비, 눈, 소나기 등 예정
  @Async
  @EventListener
  public void handleWeatherNotificationCreateEvent(WeatherNotificationCreateEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("otboo.weather_notification_create", payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka 직렬화 실패: WeatherNotificationCreateEvent for locationId={}",
          event.locationId(), e);
    } catch (Exception e) {
      log.error("Kafka 전송 실패: WeatherNotificationCreateEvent for locationId={}",
          event.locationId(), e);
    }
  }
}
