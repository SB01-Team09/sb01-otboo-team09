package com.part4.team09.otboo.module.domain.notification.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.notification.dto.NotificationDto;
import com.part4.team09.otboo.module.domain.notification.dto.NotificationDtoCursorResponse;
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
import com.part4.team09.otboo.module.domain.notification.repository.NotificationRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationRepositoryQueryDSL notificationRepositoryQueryDSL;
  private final NotificationMapper notificationMapper;

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void create(NotificationCreateRequest request) {
    log.debug("알림 생성 시작 - receiverId: {}, title: {}, content: {}, level: {}",
        request.receiverId(), request.title(), request.content(), request.level());

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

    log.debug("알림 생성 완료 - notificationId: {}", savedNotification.getId());
  }

  @Transactional
  public void createAll(NotificationCreateAllRequest request) {
    log.debug("전체 사용자 알림 생성 시작 - title: {}, content: {}, level: {}",
        request.title(), request.content(), request.level());

    List<UUID> allUserIds = userRepository.findAllIds();
    createMultiple(
        allUserIds,
        new NotificationCreateMultipleRequest(
            request.title(),
            request.content(),
            request.level()
        )
    );

    log.debug("전체 사용자 알림 생성 완료 - 대상 사용자 수: {}", allUserIds.size());
  }

  @Transactional
  public void createFollower(NotificationCreateFollowerRequest request) {
    log.debug("팔로워 대상 알림 생성 시작 - authorId: {}, title: {}, content: {}, level: {}",
        request.authorId(), request.title(), request.content(), request.level());

    List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(request.authorId());
    createMultiple(
        followerIds,
        new NotificationCreateMultipleRequest(
            request.title(),
            request.content(),
            request.level()
        )
    );

    log.debug("팔로워 대상 알림 생성 완료 - 대상 팔로워 수: {}", followerIds.size());
  }

  @Transactional
  public void createLocation(NotificationCreateLocationRequest request) {
    log.debug("지역 대상 알림 생성 시작 - locationId: {}, title: {}, content: {}, level: {}",
        request.locationId(), request.title(), request.content(), request.level());

    List<UUID> userIdsInLocation = userRepository.findUserIdsByLocationId(request.locationId());
    createMultiple(
        userIdsInLocation,
        new NotificationCreateMultipleRequest(
            request.title(),
            request.content(),
            request.level()
        )
    );

    log.debug("지역 대상 알림 생성 완료 - 대상 사용자 수: {}", userIdsInLocation.size());
  }

  // 알림 목록 조회
  @Transactional(readOnly = true)
  public NotificationDtoCursorResponse get(UUID loginUserId, String cursor, UUID idAfter,
      int limit) {
    log.debug("알림 목록 조회 시작 - loginUserId: {}, cursor: {}, idAfter: {}, limit: {}",
        loginUserId, cursor, idAfter, limit);

    // 쿼리
    // cursor을 LocalDateTime으로 디코딩
    LocalDateTime decodedCursor = decodeCursor(cursor);

    List<Notification> notifications = notificationRepositoryQueryDSL.getNotifications(loginUserId,
        decodedCursor, idAfter, limit + 1);
    int totalCount = notificationRepositoryQueryDSL.countNotifications(loginUserId);

    // Dto 리스트로 변환
    List<NotificationDto> notificationDtos = notifications.stream()
        .map(note -> notificationMapper.toDto(note))
        .toList();

    // 반환
    // hasNext
    boolean hasNext = notificationDtos.size() > limit;
    if (hasNext) {
      notificationDtos = notificationDtos.subList(0, limit);
    }

    // nextCursor, nextIdAfter
    LocalDateTime nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !notificationDtos.isEmpty()) {
      nextCursor = notificationDtos.get(limit - 1).createdAt();
      nextIdAfter = notificationDtos.get(limit - 1).id();
    }

    // nextCursor 인코딩
    String encodedNextCursor = encodeCursor(nextCursor);

    log.debug(
        "알림 목록 조회 완료 - 결과 개수: {}, hasNext: {}, nextCursor: {}, nextIdAfter: {}, totalCount: {}",
        notificationDtos.size(), hasNext, encodedNextCursor, nextIdAfter, totalCount);

    // 최종 반환
    return new NotificationDtoCursorResponse(notificationDtos, encodedNextCursor, nextIdAfter,
        hasNext, totalCount, "createdAt, id", SortDirection.DESCENDING);
  }

  @PreAuthorize("@notificationPermissionEvaluator.isNotificationReceiver(principal.id, #notificationId)")
  @Transactional
  public void delete(UUID notificationId) {
    log.debug("알림 삭제 시작 - notificationId: {}", notificationId);

    validateNotificationExists(notificationId);
    notificationRepository.deleteById(notificationId);

    log.debug("알림 삭제 완료 - notificationId: {}", notificationId);
  }

  private void createMultiple(List<UUID> receiverIds, NotificationCreateMultipleRequest request) {
    log.debug("다중 알림 생성 시작 - 대상 수: {}, title: {}, content: {}, level: {}",
        receiverIds.size(), request.title(), request.content(), request.level());

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

    log.debug("다중 알림 생성 완료 - 저장된 알림 수: {}", savedNotifications.size());
  }

  private void validateNotificationExists(UUID notificationId) {
    if (!notificationRepository.existsById(notificationId)) {
      log.debug("알림 존재하지 않음 - notificationId: {}", notificationId);
      throw NotificationNotFoundException.withId(notificationId);
    }
  }

  // cursor 인코딩 로직 (LocalDateTime -> String)
  private String encodeCursor(LocalDateTime cursor) {
    return cursor == null ? null : cursor.toString();
  }

  // cursor 디코딩 로직 (String -> LocalDateTime)
  private LocalDateTime decodeCursor(String cursor) {
    return cursor == null || cursor.isEmpty() ? null : LocalDateTime.parse(cursor);
  }
}
