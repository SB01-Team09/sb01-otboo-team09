package com.part4.team09.otboo.module.domain.auth.service;

import com.part4.team09.otboo.module.domain.auth.dto.ResetPasswordRequest;
import com.part4.team09.otboo.module.domain.auth.entity.UserTempPassword;
import com.part4.team09.otboo.module.domain.auth.event.TemporaryPasswordIssuedEvent;
import com.part4.team09.otboo.module.domain.auth.repository.UserTempPasswordRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

  @Value("${otboo.auth.temp-password.expiration-seconds}")
  private long expirationSeconds;

  private final UserRepository userRepository;
  private final UserTempPasswordRepository userTempPasswordRepository;
  private final PasswordGenerator passwordGenerator;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;

  // 임시 비밀번호 발급
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {

    String email = request.email();
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> UserNotFoundException.withEmail(email));

    // 임시 비밀번호 생성 및 인코딩
    String temporaryPassword = passwordGenerator.generate();
    String encodedPassword = passwordEncoder.encode(temporaryPassword);

    // 저장
    LocalDateTime expiresDateTime = LocalDateTime.now().plusSeconds(expirationSeconds);
    UserTempPassword userTempPassword = userTempPasswordRepository.findByUserId(user.getId())
      .map(tempPassword -> {
        tempPassword.update(encodedPassword, LocalDateTime.now(), expiresDateTime);
        return tempPassword;
      })
      .orElse(UserTempPassword.create(user.getId(), encodedPassword, expiresDateTime));
    userTempPasswordRepository.save(userTempPassword);

    // 이메일 발송
    eventPublisher.publishEvent(new TemporaryPasswordIssuedEvent(email, temporaryPassword));
  }
}
