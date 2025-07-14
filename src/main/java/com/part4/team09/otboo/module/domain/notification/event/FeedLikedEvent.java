package com.part4.team09.otboo.module.domain.notification.event;

import java.util.UUID;

public record FeedLikedEvent(
    UUID userId,
    String username,
    String feedContent
) {

}
