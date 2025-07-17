package com.part4.team09.otboo.module.domain.notification.event.Listener;

import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateFollowerRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateLocationRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import com.part4.team09.otboo.module.domain.notification.event.ClothesAttributeDefCreatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.ClothesAttributeDefUpdatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.DirectMessageReceivedEvent;
import com.part4.team09.otboo.module.domain.notification.event.FeedCommentedEvent;
import com.part4.team09.otboo.module.domain.notification.event.FeedCreatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.FeedLikedEvent;
import com.part4.team09.otboo.module.domain.notification.event.FollowedEvent;
import com.part4.team09.otboo.module.domain.notification.event.PrecipitationStartedEvent;
import com.part4.team09.otboo.module.domain.notification.event.RapidTemperatureDropEvent;
import com.part4.team09.otboo.module.domain.notification.event.RapidTemperatureRiseEvent;
import com.part4.team09.otboo.module.domain.notification.event.RoleChangedEvent;
import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  // 권한 변경
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRoleChangedEvent(RoleChangedEvent event) {
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
  }

  // 의상 속성 추가
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleClothesAttributeDefCreatedEvent(ClothesAttributeDefCreatedEvent event) {
    String title = "새로운 의상 속성이 추가되었어요.";
    String content = String.format("내 의상에 [%s] 속성을 추가해보세요.", event.name());

    NotificationCreateAllRequest request = new NotificationCreateAllRequest(
      title,
      content,
      Level.INFO
    );

    notificationService.createAll(request);
  }

  // 의상 속성 변경
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleClothesAttributeDefUpdatedEvent(ClothesAttributeDefUpdatedEvent event) {
    String title = "의상 속성이 변경되었어요.";
    String content = String.format("[%s] 속성을 확인해보세요.", event.name());

    NotificationCreateAllRequest request = new NotificationCreateAllRequest(
      title,
      content,
      Level.INFO
    );

    notificationService.createAll(request);
  }

  // 내 피드에 좋아요
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedLikedEvent(FeedLikedEvent event) {
    String title = String.format("%s님이 내 피드를 좋아합니다.", event.username());
    String content = event.feedContent();

    NotificationCreateRequest request = new NotificationCreateRequest(
      event.receiverId(),
      title,
      content,
      Level.INFO
    );

    notificationService.create(request);
  }

  // 내 피드에 댓글 등록
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedCommentedEvent(FeedCommentedEvent event) {
    String title = String.format("%s님이 댓글을 달았어요.", event.username());
    String content = event.content();

    NotificationCreateRequest request = new NotificationCreateRequest(
      event.receiverId(),
      title,
      content,
      Level.INFO
    );

    notificationService.create(request);
  }

  // 팔로우한 사용자가 피드 등록
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedCreatedEvent(FeedCreatedEvent event) {
    String title = String.format("%s님이 새로운 피드를 작성했어요.", event.authorName());
    String content = event.content();

    NotificationCreateFollowerRequest request = new NotificationCreateFollowerRequest(
      event.authorId(),
      title,
      content,
      Level.INFO
    );

    notificationService.createFollower(request);
  }

  // 다른 사용자가 나를 팔로우
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFollowedEvent(FollowedEvent event) {
    String title = String.format("%s님이 나를 팔로우했어요.", event.followerName());
    String content = "";

    NotificationCreateRequest request = new NotificationCreateRequest(
      event.receiverId(),
      title,
      content,
      Level.INFO
    );

    notificationService.create(request);
  }

  // DM 수신
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDirectMessageReceivedEvent(DirectMessageReceivedEvent event) {
    String title = String.format("[DM] %s", event.senderName());
    String content = event.content();

    NotificationCreateRequest request = new NotificationCreateRequest(
      event.receiverId(),
      title,
      content,
      Level.INFO
    );

    notificationService.create(request);
  }

  // 급격한 기온 상승 예정 (3시간 이내 5℃ 이상)
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRapidTemperatureRiseEvent(RapidTemperatureRiseEvent event) {
    String title = "3시간 이내 급격한 기온 상승이 있을 예정이에요.";
    String content = "외출 시 옷차림에 유의하세요.";

    NotificationCreateLocationRequest request = new NotificationCreateLocationRequest(
      event.locationId(),
      title,
      content,
      Level.WARNING
    );

    notificationService.createLocation(request);
  }

  // 급격한 기온 하강 예정 (3시간 이내 5℃ 이상)
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRapidTemperatureDropEvent(RapidTemperatureDropEvent event) {
    String title = "3시간 이내 급격한 기온 하강이 있을 예정이에요.";
    String content = "외출 시 옷차림에 유의하세요.";

    NotificationCreateLocationRequest request = new NotificationCreateLocationRequest(
      event.locationId(),
      title,
      content,
      Level.WARNING
    );

    notificationService.createLocation(request);
  }

  // 1시간 이내 강수 시작 예정
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePrecipitationStartedEvent(PrecipitationStartedEvent event) {
    String title = "1시간 이내 강수가 시작될 예정이에요.";
    String content = "우산을 챙기세요.";

    NotificationCreateLocationRequest request = new NotificationCreateLocationRequest(
      event.locationId(),
      title,
      content,
      Level.WARNING
    );

    notificationService.createLocation(request);
  }
}
