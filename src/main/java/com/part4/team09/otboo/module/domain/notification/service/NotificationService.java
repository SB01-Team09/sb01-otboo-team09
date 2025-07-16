package com.part4.team09.otboo.module.domain.notification.service;

import com.part4.team09.otboo.module.domain.feed.exception.feed.FeedNotFoundException;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateAllRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateFollowerRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateLocationRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateMultipleRequest;
import com.part4.team09.otboo.module.domain.notification.dto.request.NotificationCreateRequest;
import com.part4.team09.otboo.module.domain.notification.entity.Notification;
import com.part4.team09.otboo.module.domain.notification.event.NotificationCreatedEvent;
import com.part4.team09.otboo.module.domain.notification.event.NotificationCreatedMultipleEvent;
import com.part4.team09.otboo.module.domain.notification.exception.NotificationNotFoundException;
import com.part4.team09.otboo.module.domain.notification.mapper.NotificationMapper;
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void create(NotificationCreateRequest request) {
    UUID receiverId = request.receiverId();

    Notification notification = Notification.create(
        receiverId,
        request.title(),
        request.content(),
        request.level()
    );

    Notification savedNotification = notificationRepository.save(notification);

    eventPublisher.publishEvent(
        new NotificationCreatedEvent(notificationMapper.toDto(savedNotification))
    );
  }

  @Transactional
  public void createAll(NotificationCreateAllRequest request) {
    List<UUID> allUserIds = userRepository.findAllIds();

    createMultiple(
        allUserIds,
        new NotificationCreateMultipleRequest(
            request.title(),
            request.content(),
            request.level()
        )
    );
  }

  @Transactional
  public void createFollower(NotificationCreateFollowerRequest request) {
    List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(request.authorId());

    createMultiple(
        followerIds,
        new NotificationCreateMultipleRequest(
            request.title(),
            request.content(),
            request.level()
        )
    );
  }

  @Transactional
  public void createLocation(NotificationCreateLocationRequest request) {
    List<UUID> userIdsInLocation = userRepository.findUserIdsByLocationId(request.locationId());

    createMultiple(
        userIdsInLocation,
        new NotificationCreateMultipleRequest(
            request.title(),
            request.content(),
            request.level()
        )
    );
  }

  @Transactional
  public void delete(UUID notificationId) {
    validateNotificationExists(notificationId);

    notificationRepository.deleteById(notificationId);
  }

  private void createMultiple(List<UUID> receiverIds, NotificationCreateMultipleRequest request) {
    List<Notification> notifications = receiverIds.stream()
        .map(id -> Notification.create(
            id,
            request.title(),
            request.content(),
            request.level()
        ))
        .toList();

    List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

    eventPublisher.publishEvent(
        new NotificationCreatedMultipleEvent(
            savedNotifications.stream()
                .map(notificationMapper::toDto)
                .toList()
        )
    );
  }

  private void validateNotificationExists(UUID notificationId) {
    if (!notificationRepository.existsById(notificationId)) {
      throw NotificationNotFoundException.withId(notificationId);
    }
  }
}
