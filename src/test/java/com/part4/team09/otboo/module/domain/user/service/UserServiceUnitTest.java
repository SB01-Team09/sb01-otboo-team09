package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.part4.team09.otboo.module.domain.user.mapper.UserMapper;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
  @Mock
  private LocationService locationService;
  @Mock
  private LocationRepository locationRepository;
  @Mock
  private DongRepository dongRepository;

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
    when(dongRepository.findIdByLatitudeAndLongitude(anyDouble(), anyDouble()))
      .thenReturn(Optional.empty());

    // when & then
    assertThrows(LocationNotFoundException.class,
      () -> userService.updateProfile(user.getId(), updateRequest, null));
  }
}
