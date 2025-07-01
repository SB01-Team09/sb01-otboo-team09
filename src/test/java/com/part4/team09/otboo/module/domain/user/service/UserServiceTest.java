package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.part4.team09.otboo.module.domain.user.dto.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
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
  private UserService userService;

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

  @DisplayName("이메일이 사용중인 경우 로그인 실패")
  @Test
  void createUser_shouldThrowException_ifEmailAlreadyExists() {

    // given
    userRepository.save(User.createUser("test@test.com", "test", "password!"));
    UserCreateRequest request = new UserCreateRequest("name", "test@test.com", "password!");

    // when & then
    assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));
  }
}