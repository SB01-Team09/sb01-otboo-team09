package com.part4.team09.otboo.module.domain.mail.service;

import com.part4.team09.otboo.module.domain.mail.exception.EncodingMailException;
import com.part4.team09.otboo.module.domain.mail.exception.MailSendException;
import com.part4.team09.otboo.module.domain.mail.exception.MessagingMailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  @Value("${spring.mail.username}")
  private String fromMail;

  private final JavaMailSender javaMailSender;

  public String sendTemporaryPassword(String email) {
    log.info("{}의 임시 비밀번호 발급 요청", email);

    String temp_password = "임시 비밀번호";
    String title = "[Otboo] 임시 비밀번호 발급 안내";
    String content = "안녕하세요, <b>Otboo</b> 입니다." + "<br><br>"
      + "비밀번호 재설정 요청에 따라 임시 비밀번호를 발급해드립니다." + "<br><br>"
      + "<b>임시 비밀번호:</b> <span style='font-weight: bold;'>"
      + temp_password + "</span>" + "<br><br>"
      + "<b style='color: red;'>⚠️ 로그인 후 반드시 새로운 비밀번호로 변경해주세요</b>" + "<br>"
      + "<b>임시 비밀번호는 <span style='color: red;'>24시간 후 자동 만료</span>됩니다</b>" + "<br>";

    sendMail(email, title, content);

    return temp_password;
  }

  public void sendMail(String to, String title, String content) {

    MimeMessage mimeMessage = javaMailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
      helper.setFrom(fromMail, "Otboo");
      helper.setTo(to);
      helper.setSubject(title);
      helper.setText(content, true);
      javaMailSender.send(mimeMessage);
      log.info("임시 비밀번호 발급 이메일 전송 성공 (email: {})", to);

    } catch (MessagingException e) {
      log.info("이메일 전송 중 메세지 구성 실패 (email: {})", to);
      throw MessagingMailException.withEmail(to);

    } catch (UnsupportedEncodingException e) {
      log.warn("메세지 주소 인코딩 실패 (email: {})", to);
      throw EncodingMailException.withEmail(to);

    } catch (MailException e) {
      log.warn("이메일 전송 중 알 수 없는 문제 발생 (email: {})", to, e);
      throw MailSendException.withEmail(to);
    }
  }
}
