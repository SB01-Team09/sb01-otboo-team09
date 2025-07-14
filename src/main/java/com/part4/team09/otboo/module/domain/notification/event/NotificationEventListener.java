package com.part4.team09.otboo.module.domain.notification.event;

import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
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
        event.userId(),
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
        event.userId(),
        title,
        content,
        Level.INFO
    );

    notificationService.create(request);
  }

  // TODO: 내 피드에 댓글 등록


  // TODO: 팔로우한 사용자가 피드를 등록
  // TODO: 다른 사용자가 나를 팔로우
  // TODO: DM 수신
}
