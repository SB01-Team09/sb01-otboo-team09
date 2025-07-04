package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.file.FileDomain;
import com.part4.team09.otboo.module.domain.file.service.FileStorage;
import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.exception.LocationNotFoundException;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest;
import com.part4.team09.otboo.module.domain.user.dto.request.ProfileUpdateRequest.LocationUpdateRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.entity.User.Gender;
import com.part4.team09.otboo.module.domain.user.event.UserProfileUpdateEvent;
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private UserMapper userMapper;
  @Mock
  private LocationService locationService;
  @Mock
  private LocationRepository locationRepository;
  @Mock
  private DongRepository dongRepository;
  @Mock
  private FileStorage fileStorage;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private UserService userService;

  @DisplayName("프로필 조회 시 위치 정보까지 확인이 가능하다.")
  @Test
  void getProfile_success() {
    // given
    User user = User.createUser("test@test.com", "name", "password!");
    user.updateLocationId("123");

    WeatherAPILocation location = new WeatherAPILocation(1.0, 1.0, 1, 1,
      List.of("경기도", "수원시"));
    ProfileDto profileDto = new ProfileDto(user.getId(), user.getName(), Gender.MALE,
      LocalDate.now(), location, 1, null);

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(locationService.getLocation(any())).thenReturn(location);
    when(userMapper.toProfileDto(user, location)).thenReturn(profileDto);

    // when
    ProfileDto result = userService.getProfile(user.getId());

    // then
    assertThat(result).isEqualTo(profileDto);
    verify(locationService).getLocation(user.getLocationId());
    verify(userMapper).toProfileDto(user, location);
  }

  @DisplayName("카카오 api 에서 지역행정코드 조회 성공 시 위치 정보 업데이트를 성공한다.")
  @Test
  void user_profile_success() {
    // given
    User user = User.createUser("test@test.com", "name", "password!");
    user.updateLocationId("44729387");

    // 요청
    LocationUpdateRequest locationRequest = new LocationUpdateRequest(35.97664845766847,
      126.99597295767953, 59, 125, List.of("전북특별자치도", "익산시", "삼성동"));
    ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(null, null, null,
      locationRequest, null);

    // 응답
    WeatherAPILocation location = new WeatherAPILocation(locationRequest.latitude(),
      locationRequest.longitude(), 59, 125, List.of("전북특별자치도", "익산시", "삼성동"));
    ProfileDto profileDto = new ProfileDto(user.getId(), user.getName(), Gender.MALE,
      LocalDate.now(), location, 1, null);

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(locationService.getLocationCodeByCoordinates(anyDouble(), anyDouble()))
      .thenReturn("44729387");
    when(locationService.getLocation(any())).thenReturn(location);
    when(userMapper.toProfileDto(user, location)).thenReturn(profileDto);

    // when
    ProfileDto result = userService.updateProfile(user.getId(), updateRequest, null);

    // then
    assertThat(user.getLocationId()).isEqualTo("44729387");
  }

  @DisplayName("프로필 위치 정보 업데이트 시 유효하지 않은(데이터에 없는) 위치 정보일 경우 예외를 던진다.")
  @Test
  void updateProfile_shouldThrowException_ifLocationIsInvalid() {
    // given
    User user = User.createUser("test@test.com", "name", "password!");

    LocationUpdateRequest locationRequest = new LocationUpdateRequest(37.5000, 127.0364,
      60, 127, List.of("서울특별시", "강남구", "역삼동"));
    ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(null, null, null,
      locationRequest, null);

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(locationService.getLocationCodeByCoordinates(anyDouble(), anyDouble()))
      .thenReturn("44729387");
    when(locationService.getLocation("44729387"))
      .thenThrow(LocationNotFoundException.withNameAndId("location", "44729387"));

    // when & then
    assertThrows(LocationNotFoundException.class,
      () -> userService.updateProfile(user.getId(), updateRequest, null));
  }

  @Test
  @DisplayName("프로필 이미지가 업데이트되면 이미지 URL이 갱신되고, 이전 이미지 URL로 이벤트가 발행된다")
  void updateProfile_ProfileImageUpdated_PublishesEvent() {
    // given
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    MultipartFile newImage = mock(MultipartFile.class);
    ProfileDto profileDto = mock(ProfileDto.class);
    String uploadedImageUrl = "http://test.com/new-image.png";
    String previousImageUrl = "http://test.com/old-image.png";

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(user.getProfileImageUrl()).thenReturn(previousImageUrl);
    when(fileStorage.upload(newImage, FileDomain.PROFILE)).thenReturn(uploadedImageUrl);
    when(userMapper.toProfileDto(any(User.class), any())).thenReturn(profileDto);

    ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null,
      null, null);

    // when
    ProfileDto result = userService.updateProfile(userId, request, newImage);

    // then
    verify(fileStorage).upload(newImage, FileDomain.PROFILE);
    verify(user).updateProfileImageUrl(uploadedImageUrl);
    verify(eventPublisher).publishEvent(any(UserProfileUpdateEvent.class));
  }
}
