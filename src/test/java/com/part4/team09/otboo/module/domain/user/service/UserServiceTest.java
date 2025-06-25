package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.part4.team09.otboo.module.domain.user.dto.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

//@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @Test
  void create_success() {

    // given
    UserCreateRequest request = new UserCreateRequest("name", "email@test.com", "password!");

    // when
    UserDto userDto = userService.create(request);

    // then
    assertThat(userDto.email()).isEqualTo("email@test.com");
  }

  @Test
  void createUser_shouldThrowException_ifEmailAlreadyExists() {

    // given
    userRepository.save(User.createUser("test@test.com", "test", "password!"));
    UserCreateRequest request = new UserCreateRequest("name", "test@test.com", "password!");

    // when & then
    assertThrows(EmailAlreadyExistsException.class, () -> userService.create(request));
  }
}