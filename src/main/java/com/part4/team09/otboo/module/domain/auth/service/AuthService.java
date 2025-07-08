package com.part4.team09.otboo.module.domain.auth.service;

import com.part4.team09.otboo.module.common.security.jwt.AuthToken;
import com.part4.team09.otboo.module.common.security.jwt.AuthTokenRepository;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.domain.auth.exception.AccountLockedException;
import com.part4.team09.otboo.module.domain.auth.exception.InvalidTokenException;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthTokenRepository authTokenRepository;
  private final UserRepository userRepository;

  public String getAccessTokenByRefreshToken(String refreshToken) {

    // refreshToken 검증
    String userEmail = jwtTokenProvider.getSubjectFromToken(refreshToken);
    try {
      jwtTokenProvider.validateToken(refreshToken);
    } catch (AuthenticationException ex) {
      log.info("리프레시 토큰 검증 실패: {} (userEmail: {})", ex.getMessage(), userEmail);
      throw InvalidTokenException.noDetail();
    }

    // 유저 확인
    User user = userRepository.findByEmail(userEmail)
      .orElseThrow(() -> {
        log.info("토큰에 해당하는 사용자가 없습니다. (userEmail: {})", userEmail);
        return InvalidTokenException.noDetail();
      });

    // 잠금 유저 확인
    UUID userId = user.getId();
    if (!user.lock()) {
      log.info("잠금 계정이 액세스 토큰 조회 시도 (userId: {})", userId);
      throw AccountLockedException.noDetail();
    }

    // accessToken 가져오기
    AuthToken authToken = authTokenRepository.findByUserIdAndRefreshToken(userId,
        refreshToken)
      .orElseThrow(() -> {
        log.info("refreshToken에 해당하는 accessToken이 DB에 존재하지 않음 (userId: {})", userId);
        return InvalidTokenException.noDetail();
      });

    // accessToken 유효성 검사
    String accessToken = authToken.getAccessToken();
    try {
      jwtTokenProvider.validateToken(accessToken);
    } catch (AuthenticationException ex) {
      log.info("저장된 accessToken 유효성 검사 실패 (userId: {})", userId);
      throw InvalidTokenException.noDetail();
    }

    return accessToken;
  }
}
