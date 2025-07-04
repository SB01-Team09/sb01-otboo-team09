package com.part4.team09.otboo.module.domain.feed.exception;

import java.util.UUID;

public class FeedNotFoundException extends FeedException {

  public FeedNotFoundException() {
    super(FeedErrorCode.FEED_NOT_FOUND);
  }

  public static FeedNotFoundException withId(UUID id) {
    FeedNotFoundException exception = new FeedNotFoundException();
    exception.addDetail("id", id);
    return exception;
  }
}
