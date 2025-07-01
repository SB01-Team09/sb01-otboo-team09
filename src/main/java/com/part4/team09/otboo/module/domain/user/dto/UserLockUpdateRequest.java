package com.part4.team09.otboo.module.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserLockUpdateRequest(

  @NotNull(message = "잠금 상태 변경 값은 필수입니다.")
  boolean locked
) {

}
