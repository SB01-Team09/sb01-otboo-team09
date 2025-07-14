package com.part4.team09.otboo.module.domain.notification.event;

import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateFollowerRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification.Level;
import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @Async
  @EventListener
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

  @Async
  @EventListener
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

  @Async
  @EventListener
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

  @Async
  @EventListener
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

  @Async
  @EventListener
  public void handleFeedCommentedEvent(FeedCommentedEvent event) {
    String title = String.format("%s님이 댓글을 달았어요.", event.username());
    String content = event.content();

    NotificationCreateRequest request = new NotificationCreateRequest(
        event.userId(),
        title,
        content,
        Level.INFO
    );

    notificationService.create(request);
  }

  @Async
  @EventListener
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

  @Async
  @EventListener
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

  @Async
  @EventListener
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

  // TODO: 특별한 날씨 발생
}
