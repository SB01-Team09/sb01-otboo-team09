package com.part4.team09.otboo.module.common.security.oauth;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_providers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OAuthProvider extends BaseEntity {

  @Column
  private UUID userId;

  @Column
  private String provider;

  @Column
  private String providerId;

  public static OAuthProvider create(UUID userId, String provider, String providerId) {
    return new OAuthProvider(userId, provider, providerId);
  }

  private OAuthProvider(UUID userId, String provider, String providerId) {
    this.userId = userId;
    this.provider = provider;
    this.providerId = providerId;
  }

  public enum SocialType {
    GOOGLE, KAKAO
  }
}
