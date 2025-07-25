package com.part4.team09.otboo.module.domain.feed.event;

import java.util.UUID;

public record FeedDeletedEvent(
        UUID feedId
) {
}
