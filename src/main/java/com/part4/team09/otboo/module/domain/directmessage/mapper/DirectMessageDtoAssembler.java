package com.part4.team09.otboo.module.domain.directmessage.mapper;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DirectMessageDtoAssembler {

    private final UserRepository userRepository;
    private final DirectMessageMapper directMessageMapper;

    public DirectMessageDto assemble(DirectMessage directMessage, UUID userId, UUID currentUserId) {
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> UserNotFoundException.withId(currentUserId));
        UserSummary senderSummary = new UserSummary(sender.getId(), sender.getName(), sender.getProfileImageUrl());
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.withId(userId));
        UserSummary receiverSummary = new UserSummary(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl());

        return directMessageMapper.toDto(directMessage, senderSummary, receiverSummary);
    }
}
