package com.part4.team09.otboo.module.domain.auth.dto;

import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.util.UUID;

/**
 * 유저 인증 dto: 토큰이 유효할 동안 변하지 않는 값만 저장
 */
public record AuthUserDto(
  UUID userId,
  String email,
  String name,
  boolean locked,
  Role role
) {

}
