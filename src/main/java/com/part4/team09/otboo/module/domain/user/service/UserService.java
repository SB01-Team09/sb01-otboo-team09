package com.part4.team09.otboo.module.domain.user.service;

import com.part4.team09.otboo.module.domain.user.dto.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Transactional
  public UserDto create(UserCreateRequest request) {

    String email = request.email();
    checkDuplicateEmail(request.email());

    String encodedPassword = passwordEncoder.encode(request.password());
    User user = User.createUser(email, request.name(), encodedPassword);

    userRepository.save(user);

    return userMapper.toEntity(user, null);
  }

  private void checkDuplicateEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyExistsException(email);
    }
  }

}
