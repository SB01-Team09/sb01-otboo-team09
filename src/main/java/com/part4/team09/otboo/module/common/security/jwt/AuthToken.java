package com.part4.team09.otboo.module.common.security.jwt;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AuthToken extends BaseEntity {

  @Column(nullable = false, unique = true)
  UUID userId;

  @Column(columnDefinition = "TEXT")
  String accessToken;

  @Column(columnDefinition = "TEXT")
  String refreshToken;

  public static AuthToken create(UUID userId, String accessToken, String refreshToken) {
    return new AuthToken(userId, accessToken, refreshToken);
  }

  private AuthToken(UUID userId, String accessToken, String refreshToken) {
    this.userId = userId;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public void replaceToken(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
