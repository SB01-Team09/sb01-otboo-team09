package com.part4.team09.otboo.module.domain.user.mapper;

import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserSummaryMapper {

  @Mapping(source = "id", target = "userId")
  UserSummary toDto(User user);
}
