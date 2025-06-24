package com.part4.team09.otboo.module.domain.user.service;

import com.part4.team09.otboo.module.domain.user.dto.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public UserDto create(UserCreateRequest request) {

    return new UserDto(null, null, null, "name", null, null, false);
  }

}
