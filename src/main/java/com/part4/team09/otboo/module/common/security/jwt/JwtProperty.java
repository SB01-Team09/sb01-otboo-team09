package com.part4.team09.otboo.module.common.security.jwt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 설정값
 */
@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProperty {

  @NotBlank(message = "JWT 발행자는 필수입니다.")
  private final String issuer;

  @NotBlank(message = "JWT 시크릿 키는 필수입니다.")
  @Size(min = 32, message = "JWT 시크릿은 최소 32자 이상이어야 합니다.")
  private final String secret;

  @Valid
  private final TokenConfig accessToken;

  @Valid
  private final TokenConfig refreshToken;

  @Getter
  @RequiredArgsConstructor
  public static class TokenConfig {

    @Min(value = 60, message = "토큰 유효시간은 최소 60초이어야 합니다.")
    private final long validitySeconds;
  }
}
