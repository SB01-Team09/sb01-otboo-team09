package com.part4.team09.otboo.module.domain.feed.exception.like;

import com.part4.team09.otboo.module.domain.feed.exception.FeedErrorCode;
import com.part4.team09.otboo.module.domain.feed.exception.FeedException;
import java.util.UUID;

public class LikeNotFoundException extends FeedException {

  public LikeNotFoundException() {
    super(FeedErrorCode.LIKE_NOT_FOUND);
  }

  public static LikeNotFoundException withId(UUID userId, UUID feedId) {
    LikeNotFoundException exception = new LikeNotFoundException();
    exception.addDetail("userId", userId);
    exception.addDetail("feedId", feedId);
    return exception;
  }
}
