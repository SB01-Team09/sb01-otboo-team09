package com.part4.team09.otboo.module.domain.notification.event;

import java.util.UUID;

public record FeedCreatedFollowerEvent(
  UUID authorId,
  String authorName,
  String content
) {

}
