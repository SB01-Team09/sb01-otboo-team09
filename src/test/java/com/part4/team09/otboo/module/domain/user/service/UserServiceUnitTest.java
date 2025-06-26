package com.part4.team09.otboo.module.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.Optional;
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

  @Test
  void changeRole_success() {
    // given
    User user = User.createUser("test@test.com", "test", "passwd!");
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);

    // when
    UserDto dto = userService.changeRole(user.getId(), request);

    // then
    assertEquals(Role.ADMIN, user.getRole());
    verify(userMapper).toEntity(user, null);

  }
}
