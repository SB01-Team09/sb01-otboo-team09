package com.part4.team09.otboo.module.domain.directmessage.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageSendPayload;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.directmessage.mapper.DirectMessageDtoAssembler;
import com.part4.team09.otboo.module.domain.directmessage.repository.DirectMessageRepository;
import com.part4.team09.otboo.module.domain.directmessage.repository.DirectMessageRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.notification.event.DirectMessageReceivedEvent;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageService {

  private final DirectMessageRepository directMessageRepository;
  private final DirectMessageRepositoryQueryDSL directMessageRepositoryQueryDSL;
  private final DirectMessageDtoAssembler directMessageDtoAssembler;

  private final UserRepository userRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public DirectMessageSendPayload create(DirectMessageCreateRequest request) {
    log.debug("DM 생성 요청 - senderId: {}, receiverId: {}, content: {}",
        request.senderId(), request.receiverId(), request.content());

    User sender = getUserOrThrow(request.senderId());
    getUserOrThrow(request.receiverId());

    String dmKey = createDmKey(request.senderId(), request.receiverId());
    DirectMessage directMessage = DirectMessage.create(
        request.senderId(),
        request.receiverId(),
        request.content()
    );

    DirectMessage savedDirectMessage = directMessageRepository.save(directMessage);
    DirectMessageDto directMessageDto = directMessageDtoAssembler.assemble(savedDirectMessage);

    eventPublisher.publishEvent(new DirectMessageReceivedEvent(
        request.receiverId(),
        sender.getName(),
        request.content()
    ));

    log.debug("DM 저장 및 이벤트 발행 완료 - directMessageId: {}, dmKey: {}", savedDirectMessage.getId(),
        dmKey);

    return new DirectMessageSendPayload(dmKey, directMessageDto);
  }

  // DM 목록 조회
  @Transactional(readOnly = true)
  public DirectMessageDtoCursorResponse getDirectMessages(UUID userId,
      CustomUserDetails currentUser, String cursor, UUID idAfter, int limit) {
    UUID currentUserId = currentUser.getId();
    log.debug("DM 목록 조회 요청 - userId: {}, currentUserId: {}, cursor: {}, idAfter: {}, limit: {}",
        userId, currentUserId, cursor, idAfter, limit);

    // 예외처리
    getUserOrThrow(userId);

    // 쿼리
    // cursor을 LocalDateTime으로 디코딩
    LocalDateTime decodedCursor = decodeCursor(cursor);
    List<DirectMessage> directMessages = directMessageRepositoryQueryDSL.getDirectMessages(userId,
        currentUserId, decodedCursor, idAfter, limit + 1);
    int totalCount = directMessageRepositoryQueryDSL.countDirectMessages(userId, currentUserId);

    // Dto 리스트로 변환
    List<DirectMessageDto> directMessageDtos = directMessages.stream()
        .map(dm -> directMessageDtoAssembler.assemble(dm))
        .toList();

    // 반환
    // hasNext
    boolean hasNext = directMessageDtos.size() > limit;
    if (hasNext) {
      directMessageDtos = directMessageDtos.subList(0, limit);
    }

    // nextCursor, nextIdAfter
    LocalDateTime nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !directMessageDtos.isEmpty()) {
      nextCursor = directMessageDtos.get(limit - 1).createdAt();
      nextIdAfter = directMessageDtos.get(limit - 1).id();
    }

    // nextCursor 인코딩
    String encodedNextCursor = encodeCursor(nextCursor);

    log.debug("DM 목록 조회 완료 - 조회 결과 수: {}, hasNext: {}, nextCursor: {}, nextIdAfter: {}",
        directMessageDtos.size(), hasNext, encodedNextCursor, nextIdAfter);

    // 최종 반환
    return new DirectMessageDtoCursorResponse(directMessageDtos, encodedNextCursor, nextIdAfter,
        hasNext, totalCount, "createdAt, id", SortDirection.ASCENDING);
  }

  // cursor 인코딩 로직 (LocalDateTime -> String)
  private String encodeCursor(LocalDateTime cursor) {
    return cursor == null ? null : cursor.toString();
  }

  // cursor 디코딩 로직 (String -> LocalDateTime)
  private LocalDateTime decodeCursor(String cursor) {
    return cursor == null || cursor.isEmpty() ? null : LocalDateTime.parse(cursor);
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("사용자 조회 실패 - userId: {}", userId);
          return UserNotFoundException.withId(userId);
        });
  }

  private String createDmKey(UUID senderId, UUID receiverId) {
    List<UUID> ids = Arrays.asList(senderId, receiverId);
    ids.sort(Comparator.comparing(UUID::toString));
    return ids.get(0) + "_" + ids.get(1);
  }
}
