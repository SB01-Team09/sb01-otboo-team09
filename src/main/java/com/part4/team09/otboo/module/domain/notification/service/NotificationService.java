package com.part4.team09.otboo.module.domain.notification.service;

import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateFollowerRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  @Transactional
  public void create(NotificationCreateRequest request) {
    Notification notification = Notification.create(
        request.receiverId(),
        request.title(),
        request.content(),
        request.level()
    );

    notificationRepository.save(notification);
  }

  @Transactional
  public void createAll(NotificationCreateAllRequest request) {
    List<UUID> allUserIds = userRepository.findAllIds();

    List<Notification> notifications = allUserIds.stream()
        .map(id -> Notification.create(
            id,
            request.title(),
            request.content(),
            request.level()
        ))
        .toList();

    notificationRepository.saveAll(notifications);
  }

  @Transactional
  public void createFollower(NotificationCreateFollowerRequest request) {
    List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(request.authorId());

    List<Notification> notifications = followerIds.stream()
        .map(id -> Notification.create(
            id,
            request.title(),
            request.content(),
            request.level()
        ))
        .toList();

    notificationRepository.saveAll(notifications);
  }
}
