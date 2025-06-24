package com.part4.team09.otboo.module.domain.user.dto;

import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserDto(
  UUID id,
  LocalDateTime createdAt,
  String email,
  String name,
  Role role,
  List<String> linkedOAuthProviders, // TODO : 수정 예정
  boolean locked
) {

}
