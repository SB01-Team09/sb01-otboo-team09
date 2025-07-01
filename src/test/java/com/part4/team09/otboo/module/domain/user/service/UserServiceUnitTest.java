package com.part4.team09.otboo.module.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.location.service.LocationService;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
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

  @InjectMocks
  private UserService userService;

  @DisplayName("프로필 조회 정상 동작 확인")
  @Test
  void getProfile_success() {
    // given
    User user = User.createUser("test@test.com", "name", "password!");
    user.updateProfile(user.getName(), null, null, 0, "123", null);

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
}
