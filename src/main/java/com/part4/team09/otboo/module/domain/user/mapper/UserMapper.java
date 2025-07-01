package com.part4.team09.otboo.module.domain.user.mapper;

import com.part4.team09.otboo.module.domain.location.dto.response.WeatherAPILocation;
import com.part4.team09.otboo.module.domain.user.dto.ProfileDto;
import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto toDto(User user, List<String> linkedOAuthProviders);

  @Mapping(target = "location", source = "location")
  ProfileDto toProfileDto(User user, WeatherAPILocation location);
}