package com.part4.team09.otboo.module.domain.user.service;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.exception.FileUploadFailedException;
import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest.LocationUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserCreateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserLockUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.UserRoleUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import com.part4.team09.otboo.module.domain.user.event.UserProfileUpdateEvent;
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final LocationService locationService;
  private final FileStorage fileStorage;
  private final ApplicationEventPublisher eventPublisher;

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
  @Transactional(readOnly = true)
  public ProfileDto getProfile(UUID id) {
    User user = findByIdOrThrow(id);

    WeatherAPILocation location = null;
    if (user.getLocationId() != null) {
      location = locationService.getLocation(user.getLocationId());
    }
    return userMapper.toProfileDto(user, location);
  }

  // 프로필 업데이트
  @Transactional
  public ProfileDto updateProfile(UUID id, ProfileUpdateRequest request, MultipartFile image) {

    // 유저 확인
    User user = findByIdOrThrow(id);

    // 이름
    Optional.ofNullable(request.name()).ifPresent(name -> {
      if (!name.equals(user.getName())) {
        user.updateName(name);
      }
    });

    // 생일
    Optional.ofNullable(request.birthDate()).ifPresent(birthDate -> {
      if (!birthDate.equals(user.getBirthDate())) {
        user.updateBirthDate(birthDate);
      }
    });

    // 성별
    Optional.ofNullable(request.gender()).ifPresent(gender -> {
      if (!gender.equals(user.getGender())) {
        user.updateGender(gender);
      }
    });

    // 날씨 민감도
    Optional.ofNullable(request.temperatureSensitivity()).ifPresent(sensitivity -> {
      if (!sensitivity.equals(user.getTemperatureSensitivity())) {
        user.updateTemperatureSensitivity(sensitivity);
      }
    });

    // 위치
    Optional.ofNullable(request.location()).ifPresent(location -> {
      String locationId = getLocationIdByCoordinates(location);
      if (locationId != null && !locationId.equals(user.getLocationId())) {
        user.updateLocationId(locationId);
      }
    });

    // 위치 정보 가져오기
    WeatherAPILocation location = null;
    if (user.getLocationId() != null) {
      location = locationService.getLocation(user.getLocationId());
    }

    // 이미지
    String previousImageUrl = user.getProfileImageUrl();
    Optional.ofNullable(uploadProfileImage(image)).ifPresent(user::updateProfileImageUrl);

    // 프로필 이미지가 변경된 경우, 이전 프로필 이미지 삭제
    if (image != null && previousImageUrl != null) {
      log.debug("프로필 변경 이벤트 발행: {}", previousImageUrl);
      eventPublisher.publishEvent(new UserProfileUpdateEvent(previousImageUrl));
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

  /**
   * 이하 내부 유틸 / 검증 메서드
   */
  private void checkDuplicateEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      throw EmailAlreadyExistsException.withEmail(email);
    }
  }

  private User findByIdOrThrow(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> UserNotFoundException.withId(id));
  }

  private String getLocationIdByCoordinates(LocationUpdateRequest location) {
    if (location == null) {
      return null;
    }
    double latitude = location.latitude();
    double longitude = location.longitude();
    return locationService.getLocationCodeByCoordinates(longitude, latitude);
  }

  // 업로드 후 url 을 리턴하면 저장, null 인 경우(실패) 이전 값 유지
  private String uploadProfileImage(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      return null;
    }
    try {
      return fileStorage.upload(image, FileDomain.PROFILE);
    } catch (FileUploadFailedException e) {
      // TODO: 프로필 업로드 실패 알림 전송
      log.warn("{} | {}", e.getMessage(), e.getDetails());
      return null;
    }
  }
}
