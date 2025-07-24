package com.part4.team09.otboo.module.domain.auth.event;

import com.part4.team09.otboo.module.domain.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuthEventListener {

  private final EmailService emailService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void sendMailTemporaryPassword(TemporaryPasswordIssuedEvent event) {
    emailService.sendTemporaryPassword(event.email(), event.temporaryPassword());
  }
}
