package com.part4.team09.otboo.module.common.security.jwt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.part4.team09.otboo.module.common.security.exception.InvalidJwtSignatureException;
import com.part4.team09.otboo.module.common.security.exception.JwtExpiredException;
import java.time.Clock;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

  @Mock
  private AuthTokenRepository authTokenRepository;

  private JwtProperty jwtProperty;

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    JwtProperty.TokenConfig accessTokenConfig = new JwtProperty.TokenConfig(900);   // 15분
    JwtProperty.TokenConfig refreshTokenConfig = new JwtProperty.TokenConfig(604800); // 7일

    jwtProperty = new JwtProperty(
      "test-issuer",
      "test-secret-key-is-at-least-32-bytes-long-long-long",
      accessTokenConfig,
      refreshTokenConfig
    );

    jwtTokenProvider = new JwtTokenProvider(authTokenRepository, jwtProperty, Clock.systemUTC());
  }

  @Nested
  @DisplayName("JWT 유효성 검사")
  class ValidateTokenTests {

    @Test
    @DisplayName("유효한 토큰이면 통과된다.")
    void validToken_pass() throws Exception {
      // given
      String token = "test.jwt.token";

      SignedJWT mockJwt = mock(SignedJWT.class);
      JWTClaimsSet mockClaims = mock(JWTClaimsSet.class);

      try (MockedStatic<SignedJWT> jwtParser = Mockito.mockStatic(SignedJWT.class)) {
        jwtParser.when(() -> SignedJWT.parse(token)).thenReturn(mockJwt);

        when(mockJwt.verify(any())).thenReturn(true);
        when(mockJwt.getJWTClaimsSet()).thenReturn(mockClaims);
        when(mockClaims.getExpirationTime()).thenReturn(
          new Date(System.currentTimeMillis() + 60000));

        // when & then
        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));
      }
    }

    @Test
    @DisplayName("서명이 올바르지 않으면 예외가 발생한다.")
    void validToken_throwsException_IfInvalidSignature() throws Exception {
      // given
      String token = "wrong.jwt.token";

      SignedJWT mockJwt = mock(SignedJWT.class);

      try (MockedStatic<SignedJWT> mockedJwt = Mockito.mockStatic(SignedJWT.class)) {
        mockedJwt.when(() -> SignedJWT.parse(token)).thenReturn(mockJwt);

        when(mockJwt.verify(any())).thenReturn(false); // 서명 실패

        // when & then
        assertThrows(InvalidJwtSignatureException.class,
          () -> jwtTokenProvider.validateToken(token));
      }
    }

    @Test
    @DisplayName("만료된 토큰이면 만료 예외가 발생한다.")
    void validToken_throwsException_IfExpiredToken() throws Exception {
      // given
      String token = "expired.jwt.token";

      SignedJWT mockJwt = mock(SignedJWT.class);
      JWTClaimsSet mockClaims = mock(JWTClaimsSet.class);

      try (MockedStatic<SignedJWT> mockedJwt = Mockito.mockStatic(SignedJWT.class)) {
        mockedJwt.when(() -> SignedJWT.parse(token)).thenReturn(mockJwt);

        when(mockJwt.verify(any())).thenReturn(true);
        when(mockJwt.getJWTClaimsSet()).thenReturn(mockClaims);

        // 만료
        when(mockClaims.getExpirationTime()).thenReturn(
          new Date(System.currentTimeMillis() - 1000));

        // when & then
        assertThrows(JwtExpiredException.class, () -> jwtTokenProvider.validateToken(token));
      }
    }
  }
}