package com.part4.team09.otboo.module.domain.user.mapper;

import com.part4.team09.otboo.module.domain.user.dto.UserDto;
import com.part4.team09.otboo.module.domain.user.entity.User;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toEntity(User user, List<String> linkedOAuthProviders);
}