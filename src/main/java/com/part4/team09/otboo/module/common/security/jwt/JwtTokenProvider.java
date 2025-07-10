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
import com.part4.team09.otboo.module.common.security.exception.InvalidJwtFormatException;
import com.part4.team09.otboo.module.common.security.exception.InvalidJwtSignatureException;
import com.part4.team09.otboo.module.common.security.exception.JwtAuthenticationException;
import com.part4.team09.otboo.module.common.security.exception.JwtExpiredException;
import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
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
  public GeneratedToken generateToken(AuthUserDto authUserDto) {

    String accessToken = generateAccessToken(authUserDto);
    String refreshToken = generateRefreshToken(authUserDto);

    saveOrUpdateAuthToken(authUserDto.userId(), accessToken, refreshToken);

    return new GeneratedToken(accessToken, refreshToken);
  }

  // access 토큰 발행
  public String generateAccessToken(AuthUserDto authUserDto) {

    Instant now = Instant.now();
    Instant expiry = now.plusSeconds(jwtProperty.getAccessToken().getValiditySeconds());

    JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
      .issuer(jwtProperty.getIssuer())
      .subject(authUserDto.email())
      .issueTime(Date.from(now))
      .expirationTime(Date.from(expiry))
      .jwtID(UUID.randomUUID().toString())
      .claim("type", "access")
      .claim("userId", authUserDto.userId())
      .claim("name", authUserDto.name())
      .claim("email", authUserDto.email())
      .claim("role", authUserDto.role())
      .build();

    return createSignedToken(jwtClaimsSet);
  }

  // refresh 토큰 발행
  public String generateRefreshToken(AuthUserDto authUserDto) {

    Instant now = Instant.now();
    Instant expiry = now.plusSeconds(jwtProperty.getRefreshToken().getValiditySeconds());

    JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
      .issuer(jwtProperty.getIssuer())
      .subject(authUserDto.email())
      .issueTime(Date.from(now))
      .expirationTime(Date.from(expiry))
      .jwtID(UUID.randomUUID().toString())
      .claim("type", "refresh")
      .build();

    return createSignedToken(jwtClaimsSet);
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

  // 무효화
  @Transactional
  public void invalidateRefreshToken(String refreshToken) {
    authTokenRepository.deleteByRefreshToken(refreshToken);
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
