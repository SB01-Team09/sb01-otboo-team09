package com.part4.team09.otboo.module.domain.auth.mapper;

import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * UserDetails, jwt token에 들어갈 유저 정보
 */
@Mapper(componentModel = "spring")
public interface AuthUserMapper {

  @Mapping(target = "userId", source = "id")
  AuthUserDto toAuthUserDto(User user);
}
