package com.part4.team09.otboo.module.domain.user.service;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.request.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final LocationService locationService;

  @Transactional
  public UserDto createUser(UserCreateRequest request) {
    return createUserWithRole(request, User.Role.USER);
  }

  @Transactional
  public void createAdmin(UserCreateRequest request) {
    createUserWithRole(request, User.Role.ADMIN);
  }

  private UserDto createUserWithRole(UserCreateRequest request, User.Role role) {
    String email = request.email();
    checkDuplicateEmail(request.email());

    String encodedPassword = passwordEncoder.encode(request.password());
    User user = User.createUserWithRole(email, request.name(), encodedPassword, role);

    userRepository.save(user);
    return userMapper.toDto(user, null);
  }

  // 프로필 조회
  @Transactional
  public ProfileDto getProfile(UUID id) {
    User user = findByIdOrThrow(id);

    WeatherAPILocation location = null;
    if (user.getLocationId() != null) {
      location = locationService.getLocation(user.getLocationId());
    }
    return userMapper.toProfileDto(user, location);
  }

  // 권한 변경
  @Transactional
  public UserDto changeRole(UUID id, UserRoleUpdateRequest request) {
    User user = findByIdOrThrow(id);
    Role newRole = request.role();

    if (user.getRole() != newRole) {
      user.changeRole(newRole);
    }
    return userMapper.toDto(user, null);
  }

  // 잠금 상태 변경
  @Transactional
  public UserDto changeLockStatus(UUID id, @Valid UserLockUpdateRequest request) {
    User user = findByIdOrThrow(id);
    boolean locked = request.locked();

    if (locked) {
      user.lock();
    } else {
      user.unlock();
    }
    return userMapper.toDto(user, null);
  }

  private void checkDuplicateEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      throw EmailAlreadyExistsException.withEmail(email);
    }
  }

  private User findByIdOrThrow(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> UserNotFoundException.withId(id));
  }
}
