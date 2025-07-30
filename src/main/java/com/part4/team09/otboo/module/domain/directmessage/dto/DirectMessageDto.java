package com.part4.team09.otboo.module.domain.directmessage.dto;

import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(
    UUID id,
    LocalDateTime createdAt,
    UserSummary sender,
    UserSummary receiver,
    String content
) {

}
