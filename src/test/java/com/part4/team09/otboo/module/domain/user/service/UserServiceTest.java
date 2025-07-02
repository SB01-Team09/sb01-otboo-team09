package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.GuRepository;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import com.part4.team09.otboo.module.domain.location.repository.SidoRepository;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest.LocationUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
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

  @DisplayName("위치 정보 변경 시 위치 업데이트를 성공한다.")
  @Test
  @Sql(scripts = "/location-update-data.sql")
  void user_profile_success() {
    // given
    LocationUpdateRequest locationRequest = new LocationUpdateRequest(37.5000, 127.0364,
      60, 127, List.of("서울특별시", "강남구", "역삼동"));
    ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(null, null, null,
      locationRequest, null);

    // when
    ProfileDto profileDto = userService.updateProfile(user.getId(), updateRequest, null);

    // then
    User user = userRepository.findById(profileDto.userId())
      .orElseThrow(() -> new RuntimeException("User not found"));
    WeatherAPILocation location = profileDto.location();

    assertThat(location.latitude()).isEqualTo(37.5000);
    assertThat(location.longitude()).isEqualTo(127.0364);
    assertThat(user.getLocationId()).isEqualTo(
      UUID.fromString("11111111-1111-1111-1111-111111111111"));
  }
}