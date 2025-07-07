package com.part4.team09.otboo.module.common.security.jwt;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

  @Column(unique = true)
  UUID userId;

  @Column
  String refreshToken;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  LocalDateTime issuedAt;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  LocalDateTime expiredAt;

  public static RefreshToken create(UUID userId, String refreshToken, LocalDateTime issuedAt,
    LocalDateTime expiredAt) {
    return new RefreshToken(userId, refreshToken, issuedAt, expiredAt);
  }

  private RefreshToken(UUID userId, String refreshToken, LocalDateTime issuedAt,
    LocalDateTime expiredAt) {
    this.userId = userId;
    this.refreshToken = refreshToken;
    this.issuedAt = issuedAt;
    this.expiredAt = expiredAt;
  }

  public void replaceRefreshToken(String refreshToken, LocalDateTime issuedAt,
    LocalDateTime expiredAt) {
    this.refreshToken = refreshToken;
    this.issuedAt = issuedAt;
    this.expiredAt = expiredAt;
  }
}
