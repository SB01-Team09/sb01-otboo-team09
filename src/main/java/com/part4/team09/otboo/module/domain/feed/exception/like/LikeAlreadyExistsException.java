package com.part4.team09.otboo.module.domain.feed.exception.like;

import com.part4.team09.otboo.module.domain.feed.exception.FeedErrorCode;
import com.part4.team09.otboo.module.domain.feed.exception.FeedException;
import java.util.UUID;

public class LikeAlreadyExistsException extends FeedException {

  public LikeAlreadyExistsException() {
    super(FeedErrorCode.LIKE_NOT_FOUND);
  }

  public static LikeAlreadyExistsException withId(UUID userId, UUID feedId) {
    LikeAlreadyExistsException exception = new LikeAlreadyExistsException();
    exception.addDetail("userId", userId);
    exception.addDetail("feedId", feedId);
    return exception;
  }
}
