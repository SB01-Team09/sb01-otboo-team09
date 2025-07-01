package com.part4.team09.otboo.module.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  UserService userService;

  @DisplayName("유저 권한 USER에서 ADMIN으로 수정 성공")
  @Test
  void changeRole_success() {

    // given
    User user = User.createUser("test@test.com", "test", "passwd!");
    UserDto userDto = new UserDto(UUID.randomUUID(), LocalDateTime.now(), user.getEmail(),
      user.getName(), Role.ADMIN, null, user.isLocked());

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userMapper.toEntity(any(User.class), isNull())).thenReturn(userDto);

    UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);

    // when
    UserDto dto = userService.changeRole(user.getId(), request);

    // then
    assertEquals(Role.ADMIN, dto.role());
    verify(userMapper).toEntity(user, null);
  }
}
