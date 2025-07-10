package com.part4.team09.otboo.module.domain.directmessage.mapper;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {

    private final UserRepository userRepository;

    public DirectMessageDto toDto(DirectMessage directMessage) {

        User sender = userRepository.findById(directMessage.getSenderId())
                .orElseThrow(() -> UserNotFoundException.withId(directMessage.getSenderId()));
        User receiver = userRepository.findById(directMessage.getReceiverId())
                .orElseThrow(() -> UserNotFoundException.withId(directMessage.getReceiverId()));

        return new DirectMessageDto(
                directMessage.getId(),
                directMessage.getCreatedAt(),
                new UserSummary(sender.getId(), sender.getName(), sender.getProfileImageUrl()),
                new UserSummary(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl()),
                directMessage.getContent()
        );
    }

}
