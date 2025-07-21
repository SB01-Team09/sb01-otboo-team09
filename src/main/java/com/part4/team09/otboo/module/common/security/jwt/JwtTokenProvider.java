package com.part4.team09.otboo.module.common.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.part4.team09.otboo.module.common.security.exception.AccessTokenNotFoundException;
import com.part4.team09.otboo.module.common.security.exception.AccessTokenReplacedException;
import com.part4.team09.otboo.module.common.security.exception.InvalidJwtFormatException;
import com.part4.team09.otboo.module.common.security.exception.InvalidJwtSignatureException;
import com.part4.team09.otboo.module.common.security.exception.JwtAuthenticationException;
import com.part4.team09.otboo.module.common.security.exception.JwtExpiredException;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.auth.dto.TempPasswordMetadata;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JWT 필터로부터 인증 요청을 받아 실질적인 인증 수행
 * 토큰 생성 및 검증
 */
@Slf4j
@Component
public class JwtTokenProvider {

  private final AuthTokenRepository authTokenRepository;
  private final JwtProperty jwtProperty;
  private final Clock clock;

  @Autowired
  public JwtTokenProvider(JwtProperty jwtProperty, AuthTokenRepository authTokenRepository) {
    this(authTokenRepository, jwtProperty, Clock.systemUTC());
  }

  // for test
  public JwtTokenProvider(AuthTokenRepository authTokenRepository, JwtProperty jwtProperty,
    Clock clock) {
    this.authTokenRepository = authTokenRepository;
    this.jwtProperty = jwtProperty;
    this.clock = clock;

    validateSecretKey();
  }

  // 토큰 생성
  @Transactional
  public GeneratedToken generateToken(AuthUserDto authUserDto, TempPasswordMetadata tempPassword) {

    String accessToken = generateAccessToken(authUserDto, tempPassword);
    String refreshToken = generateRefreshToken(authUserDto, tempPassword);

    saveOrUpdateAuthToken(authUserDto.userId(), accessToken, refreshToken);
    return new GeneratedToken(accessToken, refreshToken);
  }

  // access 토큰 구성
  public String generateAccessToken(AuthUserDto authUserDto, TempPasswordMetadata tempPassword) {

    Instant now = Instant.now();

    Instant expiry = now.plusSeconds(jwtProperty.getAccessToken().getValiditySeconds());

    String tempPasswordExpiresAtStr = tempPassword.isUsed()
      ? tempPassword.tempPasswordExpiresAt().toString()
      : "";

    Map<String, Object> claims = new HashMap<>();
    claims.put("type", "access");
    claims.put("userId", authUserDto.userId());
    claims.put("name", authUserDto.name());
    claims.put("email", authUserDto.email());
    claims.put("role", authUserDto.role());
    claims.put("isTempPassword", tempPassword.isUsed());
    claims.put("tempPasswordExpiresAt", tempPasswordExpiresAtStr);

    return generateToken(authUserDto.email(), expiry, claims);
  }

  // refresh 토큰 구성
  public String generateRefreshToken(AuthUserDto authUserDto, TempPasswordMetadata tempPassword) {

    Instant now = Instant.now();

    Instant expiry = tempPassword.isUsed()
      ? tempPassword.expiresAtAsInstant()
      : now.plusSeconds(jwtProperty.getRefreshToken().getValiditySeconds());

    String tempPasswordExpiresAtStr = tempPassword.isUsed()
      ? tempPassword.tempPasswordExpiresAt().toString()
      : "";

    log.debug("임시 비밀번호 생성: {}", tempPassword.isUsed());
    log.debug("refresh token 생성 시간: {}", LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
    log.debug("refresh token 만료 시간: {}", LocalDateTime.ofInstant(expiry, ZoneId.systemDefault()));

    Map<String, Object> claims = new HashMap<>();
    claims.put("type", "refresh");
    claims.put("isTempPassword", tempPassword.isUsed());
    claims.put("tempPasswordExpiresAt", tempPasswordExpiresAtStr);

    return generateToken(authUserDto.email(), expiry, claims);
  }

  // 토큰 생성
  public String generateToken(String subject, Instant expiresAt, Map<String, Object> claims) {

    Instant now = Instant.now();

    JWTClaimsSet.Builder jwtClaimsSet = new JWTClaimsSet.Builder()
      .issuer(jwtProperty.getIssuer())
      .subject(subject)
      .issueTime(Date.from(now))
      .expirationTime(Date.from(expiresAt))
      .jwtID(UUID.randomUUID().toString());

    claims.forEach(jwtClaimsSet::claim);

    return createSignedToken(jwtClaimsSet.build());
  }

  // 동시 로그인 제한 및 유효성 검증
  public void validateTokenWithSession(String accessToken) {
    // 유효성
    validateToken(accessToken);

    // 동시 로그인 제한
    UUID userId = getAuthUserDtoFromToken(accessToken).userId();
    authTokenRepository.findByUserId(userId)
      .ifPresentOrElse(
        authToken -> {
          if (!authToken.getAccessToken().equals(accessToken)) {
            log.info("이전 토큰으로 인증 시도 (userId: {})", userId);
            throw new AccessTokenReplacedException("다른 세션에서 로그인되어 기존 토큰은 만료되었습니다.");
          }
        },
        () -> {
          log.info("로그아웃된 토큰으로 인증 시도 (userId: {})", userId);
          throw new AccessTokenNotFoundException("토큰이 만료되었습니다. 다시 로그인하세요.");
        }
      );
  }

  // 토큰 유효성 검증
  public void validateToken(String token) {
    try {
      // jwt 문자열을 SignedJwt로 파싱
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 서명 검증을 위한 verifier 생성: 시크릿 키로 MACVerifier 초기화
      JWSVerifier verifier = new MACVerifier(getSingingKey());

      // 서명 검증
      if (!signedJWT.verify(verifier)) {
        throw new InvalidJwtSignatureException("JWT 서명이 유효하지 않습니다.");
      }

      // 토큰 만료 확인
      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expirationTime.before(new Date())) {
        throw new JwtExpiredException("토큰이 만료되었습니다.");
      }

    } catch (ParseException e) {
      throw new InvalidJwtFormatException("JWT 형식이 잘못되었습니다.");
    } catch (JOSEException e) {
      throw new JwtAuthenticationException("JWT 처리 중 오류가 발생했습니다.", e);
    }
  }

  // 클레임에서 유저 정보 추출
  public AuthUserDto getAuthUserDtoFromToken(String token) throws AuthenticationException {
    try {
      JWTClaimsSet claimsSet = parseToken(token);
      UUID userId = UUID.fromString(claimsSet.getClaimAsString("userId"));
      String email = claimsSet.getClaimAsString("email");
      String name = claimsSet.getClaimAsString("name");
      Role role = Role.valueOf(claimsSet.getClaim("role").toString());

      return new AuthUserDto(userId, email, name, false, role);

    } catch (ParseException e) {
      throw new InvalidJwtFormatException("JWT 형식이 잘못되었습니다.");
    }
  }

  // 클레임에서 유저 subject 추출
  public String getSubjectFromToken(String token) throws AuthenticationException {
    JWTClaimsSet claimsSet = parseToken(token);
    return claimsSet.getSubject();
  }

  // 클레임에서 임시 비밀번호 정보 추출
  public TempPasswordMetadata getTempPasswordMetaDataFromToken(String token) {
    try {
      JWTClaimsSet claimsSet = parseToken(token);
      boolean isUsed = claimsSet.getBooleanClaim("isTempPassword");
      String expiresAtStr = claimsSet.getStringClaim("tempPasswordExpiresAt");
      LocalDateTime expiresAt = expiresAtStr.isBlank() ? null : LocalDateTime.parse(expiresAtStr);

      return new TempPasswordMetadata(isUsed, expiresAt);

    } catch (ParseException e) {
      throw new InvalidJwtFormatException("JWT 형식이 잘못되었습니다.");
    }
  }

  // 무효화
  @Transactional
  public void invalidateRefreshToken(String refreshToken) {
    authTokenRepository.deleteByRefreshToken(refreshToken);
  }

  public Authentication getAuthentication(String token) {
    AuthUserDto userDto = getAuthUserDtoFromToken(token);
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userDto.role().name());

    return new UsernamePasswordAuthenticationToken(
        userDto, // Principal
        null,    // credentials (비밀번호 null)
        List.of(authority)
    );
  }

  // 서명
  private String createSignedToken(JWTClaimsSet jwtClaimsSet) {
    try {
      // 헤더 설정
      JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS256)
        .type(JOSEObjectType.JWT)
        .build();

      // 서명
      SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
      JWSSigner signer = new MACSigner(getSingingKey());
      signedJWT.sign(signer);

      return signedJWT.serialize();

    } catch (JOSEException e) {
      log.warn("Refresh 토큰 생성 중 JOSEException 발생: {}", e.getMessage());
      throw new JwtAuthenticationException("JWT 생성 중 오류가 발생했습니다.", e);
    }
  }

  // refreshToken 저장 및 업데이트
  private void saveOrUpdateAuthToken(UUID userId, String accessToken, String refreshToken) {

    AuthToken tokenEntity = authTokenRepository.findByUserId(userId)
      .map(token -> {
        token.replaceToken(accessToken, refreshToken);
        return token;
      })
      .orElseGet(() -> AuthToken.create(userId, accessToken, refreshToken));

    authTokenRepository.save(tokenEntity);
  }

  // 클레임 추출
  private JWTClaimsSet parseToken(String token) throws AuthenticationException {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet();
    } catch (ParseException e) {
      throw new InvalidJwtFormatException("JWT 형식이 잘못되었습니다.");
    }
  }

  // 시크릿 키 생성
  private SecretKey getSingingKey() {
    byte[] keyBytes = jwtProperty.getSecret().getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, "HmacSHA256");
  }

  // 키 검사
  private void validateSecretKey() {
    String secret = jwtProperty.getSecret();
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException("JWT 시크릿 키는 32자 이상이어야 합니다.(길이: {})" +
        (secret != null ? secret.length() : 0)
      );
    }
  }
}
