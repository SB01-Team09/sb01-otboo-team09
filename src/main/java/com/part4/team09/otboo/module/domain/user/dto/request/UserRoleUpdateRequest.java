package com.part4.team09.otboo.module.domain.user.dto.request;

import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(

  @NotNull(message = "변경할 역할을 입력해야 합니다.")
  Role role
) {

}
