package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.GuRepository;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import com.part4.team09.otboo.module.domain.location.repository.SidoRepository;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.request.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private LocationRepository locationRepository;
  @Autowired
  private SidoRepository sidoRepository;
  @Autowired
  private GuRepository guRepository;
  @Autowired
  private DongRepository dongRepository;
  @Autowired
  private UserService userService;

  private User user;

  @BeforeEach
  void setUp() {
    User testUser = User.createUser("testUser@test.com", "test", "password!");
    user = userRepository.save(testUser);
  }

  @DisplayName("로그인 성공 테스트")
  @Test
  void create_success() {

    // given
    UserCreateRequest request = new UserCreateRequest("name", "email@test.com", "password!");

    // when
    UserDto userDto = userService.createUser(request);

    // then
    assertThat(userDto.email()).isEqualTo("email@test.com");
  }

  @DisplayName("이메일이 사용중인 경우 계정 생성 실패")
  @Test
  void createUser_shouldThrowException_ifEmailAlreadyExists() {

    // given
    UserCreateRequest request = new UserCreateRequest("name", user.getEmail(), "password!");

    // when & then
    assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));
  }

  @DisplayName("유저 권한 변경 성공")
  @Test
  void change_role_success() {
    // given
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);

    // when
    UserDto userDto = userService.changeRole(user.getId(), request);

    // then
    assertThat(userDto.role()).isEqualTo(Role.ADMIN);
  }

  @DisplayName("유저 잠금 상태 변경 성공")
  @Test
  void change_lock_status_success() {
    // given
    UserLockUpdateRequest request = new UserLockUpdateRequest(true);

    // when
    UserDto userDto = userService.changeLockStatus(user.getId(), request);

    // then
    assertThat(userDto.locked()).isEqualTo(true);
  }
}