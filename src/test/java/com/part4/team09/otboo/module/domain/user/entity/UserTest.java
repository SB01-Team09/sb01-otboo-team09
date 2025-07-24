package com.part4.team09.otboo.module.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserTest {

  @DisplayName("유저 잠금 시 잠금 상태가 변경된 경우 true 반환")
  @Test
  void lock_returnsTrue_ifUserIsUnlocked() {

    // given
    User user = User.createUser("test@test.com", "name", "password!");

    // when
    boolean isChanged = user.lock();

    // then
    assertThat(isChanged).isEqualTo(true);
    assertThat(user.isLocked()).isEqualTo(true);
  }

  @DisplayName("유저 잠금 시 유저가 이미 잠금 상태인 경우 false 반환")
  @Test
  void lock_returnsFalse_ifUserIsAlreadyUnlocked() {

    // given
    User user = User.createUser("test@test.com", "name", "password!");
    user.lock();

    // when
    boolean isChanged = user.lock();

    // then
    assertThat(isChanged).isEqualTo(false);
    assertThat(user.isLocked()).isEqualTo(true);
  }

  @DisplayName("유저 잠금 해제 시 잠금 상태가 변경된 경우 true 반환")
  @Test
  void lock_returnsTrue_ifUserIsLocked() {

    // given
    User user = User.createUser("test@test.com", "name", "password!");
    user.lock();

    // when
    boolean isChanged = user.unlock();

    // then
    assertThat(isChanged).isEqualTo(true);
    assertThat(user.isLocked()).isEqualTo(false);
  }

  @DisplayName("유저 잠금 해제 시 유저가 이미 잠금 해제 상태인 경우 false 반환")
  @Test
  void lock_returnsFalse_ifUserIsAlreadyLocked() {

    // given
    User user = User.createUser("test@test.com", "name", "password!");

    // when
    boolean isChanged = user.unlock();

    // then
    assertThat(isChanged).isEqualTo(false);
    assertThat(user.isLocked()).isEqualTo(false);
  }
}