package com.part4.team09.otboo.module.domain.directmessage.mapper;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.mapper.UserSummaryMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageDtoAssembler {

  private final DirectMessageMapper directMessageMapper;

  private final UserRepository userRepository;
  private final UserSummaryMapper userSummaryMapper;

  public DirectMessageDto assemble(DirectMessage directMessage) {
    User sender = getUserOrThrow(directMessage.getSenderId());
    UserSummary senderSummary = userSummaryMapper.toDto(sender);

    User receiver = getUserOrThrow(directMessage.getReceiverId());
    UserSummary receiverSummary = userSummaryMapper.toDto(receiver);

    return directMessageMapper.toDto(directMessage, senderSummary, receiverSummary);
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }
}
