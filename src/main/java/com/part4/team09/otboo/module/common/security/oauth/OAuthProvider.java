package com.part4.team09.otboo.module.common.security.oauth;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 정보
 */
@Entity
@Table(name = "oauth_providers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OAuthProvider extends BaseEntity {

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SocialType provider;

  @Column(nullable = false)
  private String providerId;

  public static OAuthProvider create(UUID userId, SocialType provider, String providerId) {
    return new OAuthProvider(userId, provider, providerId);
  }

  private OAuthProvider(UUID userId, SocialType provider, String providerId) {
    this.userId = userId;
    this.provider = provider;
    this.providerId = providerId;
  }

  public enum SocialType {
    GOOGLE, KAKAO
  }
}
