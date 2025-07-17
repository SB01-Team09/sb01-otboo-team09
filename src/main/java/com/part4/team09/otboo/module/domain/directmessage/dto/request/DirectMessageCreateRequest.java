package com.part4.team09.otboo.module.domain.directmessage.dto.request;

import java.util.UUID;

public record DirectMessageCreateRequest(
    UUID receiverId,
    UUID senderId,
    String content
) {

}
