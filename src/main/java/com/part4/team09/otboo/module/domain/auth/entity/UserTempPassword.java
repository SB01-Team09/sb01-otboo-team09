package com.part4.team09.otboo.module.domain.auth.entity;

import com.part4.team09.otboo.module.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_temp_passwords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTempPassword extends BaseEntity {

  @Column(nullable = false, unique = true)
  private UUID userId;

  @Column(nullable = false)
  private String temporaryPassword;

  @Column(nullable = false)
  private LocalDateTime issuedAt;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  public static UserTempPassword create(UUID userId, String temporaryPassword,
    LocalDateTime expiresAt) {
    return new UserTempPassword(userId, temporaryPassword, LocalDateTime.now(), expiresAt);
  }

  private UserTempPassword(UUID userId, String temporaryPassword, LocalDateTime issuedAt,
    LocalDateTime expiresAt) {
    this.userId = userId;
    this.temporaryPassword = temporaryPassword;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }

  public void update(String temporaryPassword, LocalDateTime issuedAt, LocalDateTime expiresAt) {
    this.temporaryPassword = temporaryPassword;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired(LocalDateTime now) {
    return now.isAfter(this.expiresAt);
  }
}
