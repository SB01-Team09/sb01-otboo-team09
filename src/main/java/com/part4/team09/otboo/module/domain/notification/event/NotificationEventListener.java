package com.part4.team09.otboo.module.domain.notification.event;

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

  // TODO: 의상 속성 추가
  // TODO: 내 피드에 좋아요 또는 댓글 등록
  // TODO: 팔로우한 사용자가 피드를 등록
  // TODO: 다른 사용자가 나를 팔로우
  // TODO: DM 수신
}
