package com.part4.team09.otboo.module.domain.feed.mapper;

import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

  @Mapping(source = "id", target = "userId")
  AuthorDto toDto(User user);
}
