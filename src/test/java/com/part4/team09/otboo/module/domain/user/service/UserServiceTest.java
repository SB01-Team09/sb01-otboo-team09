package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.GuRepository;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import com.part4.team09.otboo.module.domain.location.repository.SidoRepository;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDtoCursorResponse;
import com.part4.team09.otboo.module.domain.user.dto.request.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserListRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import com.part4.team09.otboo.module.domain.user.repository.UserRepositoryQueryDSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
  @MockitoBean
  private UserRepositoryQueryDSL userRepositoryQueryDSL;
  @MockitoBean
  private UserMapper userMapper;

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

  @DisplayName("계정 목록 조회 성공")
  @Test
  void getUsersSuccess() {
    // given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    User user1 = User.createUser("yg@naver.com", "연경", "password");
    User user2 = User.createAdmin("ke@naver.com", "가은", "passwordtoo");

    List<User> Users = new ArrayList<>(List.of(user1, user2));
    Users.add(User.createUser("es@naver.com", "은수", "pass"));

    UserDto userDto1 = new UserDto(id1, LocalDateTime.now(), "yg@naver.com", "연경", User.Role.USER, List.of(), false);
    UserDto userDto2 = new UserDto(id2, LocalDateTime.now(), "ke@naver.com", "가은", User.Role.ADMIN, List.of(), false);

    UserListRequest request = new UserListRequest(
            null, null, 2, "email", SortDirection.ASCENDING, null, null, null
    );

    when(userRepositoryQueryDSL.getUsers(request)).thenReturn(Users);
    when(userRepositoryQueryDSL.countUsers(request)).thenReturn(3);
    when(userMapper.toDto(user1, null)).thenReturn(userDto1);
    when(userMapper.toDto(user2, null)).thenReturn(userDto2);

    // when
    UserDtoCursorResponse response = userService.getUsers(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.data()).hasSize(2);
    assertThat(response.totalCount()).isEqualTo(3);
    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextCursor()).isEqualTo(user2.getEmail());
    assertThat(response.sortBy()).isEqualTo("email");
    assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
  }

  @DisplayName("계정 목록 조회 성공: hasNext = false")
  @Test
  void getUsersHasNextFalse() {
    // given
    User user = User.createUser("hm@naver.com", "혜민", "pw");
    UserDto userDto = new UserDto(UUID.randomUUID(), LocalDateTime.now(), user.getEmail(), user.getName(), user.getRole(), List.of(), user.isLocked());

    UserListRequest request = new UserListRequest(
            null, null, 2, "email", SortDirection.ASCENDING, null, null, null
    );

    when(userRepositoryQueryDSL.getUsers(request)).thenReturn(List.of(user));
    when(userRepositoryQueryDSL.countUsers(request)).thenReturn(1);
    when(userMapper.toDto(user, null)).thenReturn(userDto);

    // when
    UserDtoCursorResponse response = userService.getUsers(request);

    // then
    assertThat(response.hasNext()).isFalse();
    assertThat(response.data()).hasSize(1);
    assertThat(response.totalCount()).isEqualTo(1);
    assertThat(response.nextCursor()).isNull();
    assertThat(response.nextIdAfter()).isNull();
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