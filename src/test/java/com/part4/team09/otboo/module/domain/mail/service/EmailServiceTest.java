package com.part4.team09.otboo.module.domain.mail.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.mail.exception.MailSendException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock
  private JavaMailSender javaMailSender;

  @InjectMocks
  private EmailService emailService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailService, "fromMail", "test@otboo.com");
  }

  @Test
  @DisplayName("임시 비밀번호 메일 전송 성공")
  void sendTemporaryPassword_success() throws Exception {
    // given
    MimeMessage mockMessage = mock(MimeMessage.class);
    when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);

    // when
    emailService.sendTemporaryPassword("test@example.com", "temp1234");

    // then
    verify(javaMailSender).send(mockMessage);
  }

  @Test
  @DisplayName("MailException 발생 시 예외 던짐")
  void sendMail_mailException_throwsCustomException() throws Exception {
    // given
    MimeMessage mockMessage = mock(MimeMessage.class);
    when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
    doThrow(new MailSendException()).when(javaMailSender).send(any(MimeMessage.class));

    // when & then
    assertThrows(MailSendException.class,
      () -> emailService.sendMail("test@example.com", "제목", "내용")
    );
  }
}