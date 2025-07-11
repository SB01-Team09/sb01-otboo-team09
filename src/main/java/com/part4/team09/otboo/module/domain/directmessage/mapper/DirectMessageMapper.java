package com.part4.team09.otboo.module.domain.directmessage.mapper;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {

    public DirectMessageDto toDto(DirectMessage directMessage, UserSummary senderSummary, UserSummary receiverSummary) {

        return new DirectMessageDto(
                directMessage.getId(),
                directMessage.getCreatedAt(),
                senderSummary,
                receiverSummary,
                directMessage.getContent()
        );
    }

}
