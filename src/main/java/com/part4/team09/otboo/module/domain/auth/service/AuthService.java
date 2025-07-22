package com.part4.team09.otboo.module.domain.auth.service;

import com.part4.team09.otboo.module.common.security.jwt.AuthToken;
import com.part4.team09.otboo.module.common.security.jwt.AuthTokenRepository;
import com.part4.team09.otboo.module.common.security.jwt.GeneratedToken;
import com.part4.team09.otboo.module.common.security.jwt.JwtTokenProvider;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.dto.TempPasswordMetadata;
import com.part4.team09.otboo.module.domain.auth.exception.AccountLockedException;
import com.part4.team09.otboo.module.domain.auth.exception.InvalidTokenException;
import com.part4.team09.otboo.module.domain.auth.mapper.AuthUserMapper;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * 토큰 인증 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthTokenRepository authTokenRepository;
  private final UserRepository userRepository;
  private final AuthUserMapper authUserMapper;

  public String getAccessTokenByRefreshToken(String refreshToken) {

    // refreshToken 검증
    String userEmail = jwtTokenProvider.getSubjectFromToken(refreshToken);
    validateRefreshTokenOrThrow(userEmail, refreshToken);

    // 유저 확인
    User user = findUserByEmailOrThrow(userEmail);

    // 잠금 유저 확인
    checkUserNotLockedOrThrow(user);

    // accessToken 가져오기
    AuthToken authToken = findAuthTokenByUserIdAndRefreshTokenOrThrow(user.getId(), refreshToken);

    // accessToken 유효성 검사
    String accessToken = authToken.getAccessToken();
    try {
      jwtTokenProvider.validateToken(accessToken);
    } catch (AuthenticationException ex) {
      log.info("저장된 accessToken 유효성 검사 실패 (userId: {})", user.getId());
      throw InvalidTokenException.noDetail();
    }
    return accessToken;
  }

  public GeneratedToken refreshTokens(String refreshToken) {
    // 유효성 검사
    String userEmail = jwtTokenProvider.getSubjectFromToken(refreshToken);
    validateRefreshTokenOrThrow(userEmail, refreshToken);

    // 유저 확인
    User user = findUserByEmailOrThrow(userEmail);

    // 잠금 확인
    checkUserNotLockedOrThrow(user);

    // token 정보 확인
    findAuthTokenByUserIdAndRefreshTokenOrThrow(user.getId(), refreshToken);

    // 토큰에서 임시 비밀번호 정보 가져오기
    TempPasswordMetadata tempPassword = jwtTokenProvider.getTempPasswordMetaDataFromToken(
      refreshToken);

    AuthUserDto authUserDto = authUserMapper.toAuthUserDto(user);

    return jwtTokenProvider.generateToken(authUserDto, tempPassword);
  }

  public void forceLogout(UUID userId) {
    authTokenRepository.deleteByUserId(userId);
  }

  // 리프레시 토큰 검증
  private void validateRefreshTokenOrThrow(String userEmail, String refreshToken) {
    try {
      jwtTokenProvider.validateToken(refreshToken);
    } catch (AuthenticationException ex) {
      log.info("리프레시 토큰 검증 실패: {} (userEmail: {})", ex.getMessage(), userEmail);
      throw InvalidTokenException.noDetail();
    }
  }

  // 인증 토큰 가져오기
  private AuthToken findAuthTokenByUserIdAndRefreshTokenOrThrow(UUID userId, String refreshToken) {
    return authTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken)
      .orElseThrow(() -> {
        log.info("refreshToken에 해당하는 인증 토큰이 존재하지 않습니다. (userId: {})", userId);
        return InvalidTokenException.noDetail();
      });
  }

  // 유저 정보 가져오기
  private User findUserByEmailOrThrow(String userEmail) {
    return userRepository.findByEmail(userEmail)
      .orElseThrow(() -> {
        log.info("토큰에 해당하는 사용자가 없습니다. (userEmail: {})", userEmail);
        return InvalidTokenException.noDetail();
      });
  }

  // 잠금 여부 체크
  private void checkUserNotLockedOrThrow(User user) {
    if (!user.lock()) {
      log.info("잠금 계정이 액세스 토큰 조회 시도 (userId: {})", user.getId());
      throw AccountLockedException.noDetail();
    }
  }
}
