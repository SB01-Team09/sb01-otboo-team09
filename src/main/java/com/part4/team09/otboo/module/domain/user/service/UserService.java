package com.part4.team09.otboo.module.domain.user.service;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.exception.FileUploadFailedException;
import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.exception.LocationNotFoundException;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
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
import com.part4.team09.otboo.module.domain.user.exception.EmailAlreadyExistsException;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
  private final DongRepository dongRepository;
  private final LocationRepository locationRepository;

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

  // 프로필 업데이트
  @Transactional
  public ProfileDto updateProfile(UUID id, ProfileUpdateRequest request, MultipartFile image) {
    // 유저 확인
    User user = findByIdOrThrow(id);

    // 위치 업데이트
    WeatherAPILocation location = null;
    String locationId = findLocationOrThrow(request.location());
    if (locationId != null && !locationId.equals(user.getLocationId())) {
      location = locationService.getLocation(locationId);
    }

    // 이미지 업로드 동기 처리 -> 실패하면 이전 프로필로 유지
    // null이 아니면 업데이트
    String imageUrl = uploadProfileImage(image);

    // 이름, 성별, 생일, 민감도, 위치, 프로필 업데이트
    //updateProfile -> null, 같은 값인지 검사
    // 구조 변경 고려 : 값이 같을 때 dirty checking 확인
    user.updateProfile(user.getName(), user.getGender(), user.getBirthDate(),
      user.getTemperatureSensitivity(), locationId, imageUrl);

    // 업데이트 후 userProfile 반환
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

  private String findLocationOrThrow(LocationUpdateRequest location) {
    if (location == null) {
      return null;
    }
    double latitude = location.latitude();
    double longitude = location.longitude();
    UUID dongId = dongRepository.findIdByLatitudeAndLongitude(latitude, longitude)
      .orElseThrow(() -> LocationNotFoundException.withLatitudeAndLongitude(latitude, longitude));
    return locationRepository.findIdByDongId(dongId)
      .orElseThrow(() -> LocationNotFoundException.withNameAndId("dong", dongId));
  }

  // 업로드 후 url 을 리턴하면 저장, null 인 경우(실패) 이전값 유지
  private String uploadProfileImage(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      log.info("파일 없음");
      return null;
    }
    try {
      log.info("파일 업로드 시작-user");
      return fileStorage.upload(image, FileDomain.PROFILE);
    } catch (FileUploadFailedException e) {

      // 프로필 업로드 실패 알림 전송
      log.info("파일 업로드 실패-user");
      log.warn("{} | {}", e.getMessage(), e.getDetails());
      return null;
    }
  }
}
