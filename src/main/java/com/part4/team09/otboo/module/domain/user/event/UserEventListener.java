package com.part4.team09.otboo.module.domain.user.event;

import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

  private final FileStorage fileStorage;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void removePreviousProfileFile(UserProfileUpdateEvent event) {
    fileStorage.remove(event.previousImageUrl());
  }
}
