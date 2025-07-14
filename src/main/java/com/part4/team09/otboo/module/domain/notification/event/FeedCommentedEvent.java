package com.part4.team09.otboo.module.domain.notification.event;

import java.util.UUID;

public record FeedCommentedEvent(
    UUID userId,
    String username,
    String content
) {

}
