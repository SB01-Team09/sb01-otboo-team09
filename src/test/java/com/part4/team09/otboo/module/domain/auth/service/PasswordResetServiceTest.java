package com.part4.team09.otboo.module.domain.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.auth.dto.ResetPasswordRequest;
import com.part4.team09.otboo.module.domain.auth.entity.UserTempPassword;
import com.part4.team09.otboo.module.domain.auth.event.TemporaryPasswordIssuedEvent;
import com.part4.team09.otboo.module.domain.auth.repository.UserTempPasswordRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserTempPasswordRepository userTempPasswordRepository;
  @Mock
  private PasswordGenerator passwordGenerator;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private ApplicationEventPublisher eventPublisher;


  @InjectMocks
  private PasswordResetService passwordResetService;

  @Test
  @DisplayName("유효한 임시 비밀번호가 없을 때 새로 생성되며 임시 비밀번호 발급이 성공한다.")
  void resetPassword_success_ifNoneExists() {
    // given
    User user = User.createUser("test@test.com", "test", "password!");

    String rawTempPassword = "tempPassword1234";
    String encodedPassword = "encodedTempPassword1234";

    ResetPasswordRequest request = new ResetPasswordRequest(user.getEmail());
    TemporaryPasswordIssuedEvent event = new TemporaryPasswordIssuedEvent(user.getEmail(),
      rawTempPassword);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userTempPasswordRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
    when(passwordGenerator.generate()).thenReturn(rawTempPassword);
    when(passwordEncoder.encode(rawTempPassword)).thenReturn(encodedPassword);

    // when
    passwordResetService.resetPassword(request);

    // then
    verify(userRepository).findByEmail(user.getEmail());
    verify(userTempPasswordRepository).findByUserId(user.getId());
    verify(passwordGenerator).generate();
    verify(passwordEncoder).encode(rawTempPassword);
    verify(userTempPasswordRepository).save(any(UserTempPassword.class));
    verify(eventPublisher).publishEvent(event);
  }

  @Test
  @DisplayName("유효한 임시 비밀번호가 이미 있을 때 업데이트하며 임시 비밀번호 발급이 성공한다.")
  void resetPassword_success_whenAlreadyExists() {
    // given
    User user = User.createUser("test@test.com", "test", "password!");

    String rawTempPassword = "tempPassword1234";
    String encodedPassword = "encodedTempPassword1234";

    ResetPasswordRequest request = new ResetPasswordRequest(user.getEmail());
    UserTempPassword existingTempPassword = mock(UserTempPassword.class);
    TemporaryPasswordIssuedEvent event = new TemporaryPasswordIssuedEvent(user.getEmail(),
      rawTempPassword);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userTempPasswordRepository.findByUserId(user.getId()))
      .thenReturn(Optional.of(existingTempPassword));
    when(passwordGenerator.generate()).thenReturn(rawTempPassword);
    when(passwordEncoder.encode(rawTempPassword)).thenReturn(encodedPassword);
    when(userTempPasswordRepository.save(existingTempPassword)).thenReturn(existingTempPassword);

    // when
    passwordResetService.resetPassword(request);

    // then
    verify(existingTempPassword).update(anyString(), any(), any());// encodedPw, issuedAt, expiresAt
    verify(userTempPasswordRepository).save(existingTempPassword);
    verify(eventPublisher).publishEvent(event);
  }

}