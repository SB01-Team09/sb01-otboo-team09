package com.part4.team09.otboo.module.domain.auth.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * 임시 비밀번호 생성기
 */
@Component
public class PasswordGenerator {

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final SecureRandom random = new SecureRandom();

  public String generate() {
    return generate(10);
  }

  public String generate(int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }
}
