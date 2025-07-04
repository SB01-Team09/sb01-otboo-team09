package com.part4.team09.otboo.module.domain.user.entity;

import com.part4.team09.otboo.module.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseUpdatableEntity {

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false)
  private boolean locked;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private LocalDate birthDate;

  private int temperatureSensitivity;

  private String locationId;

  private String profileImageUrl;

  public enum Role {
    USER, ADMIN
  }

  public enum Gender {
    MALE, FEMALE, OTHER
  }

  public static User createUser(String email, String name, String password) {
    return new User(email, name, password, Role.USER);
  }

  public static User createAdmin(String email, String name, String password) {
    return new User(email, name, password, Role.ADMIN);
  }

  public static User createUserWithRole(String email, String name, String password, Role role) {
    return new User(email, name, password, role);
  }

  private User(String email, String name, String password, Role role) {
    this.email = email;
    this.name = name;
    this.password = password;
    this.role = role;
    this.locked = false;
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateGender(Gender gender) {
    this.gender = gender;
  }

  public void updateBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public void updateTemperatureSensitivity(int temperatureSensitivity) {
    this.temperatureSensitivity = temperatureSensitivity;
  }

  public void updateLocationId(String locationId) {
    this.locationId = locationId;
  }

  public void updateProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  public void changeRole(Role newRole) {
    this.role = newRole;
  }

  public boolean lock() {
    if (!this.locked) {
      this.locked = true;
      return true;
    }
    return false;
  }

  public boolean unlock() {
    if (this.locked) {
      this.locked = false;
      return true;
    }
    return false;
  }
}
