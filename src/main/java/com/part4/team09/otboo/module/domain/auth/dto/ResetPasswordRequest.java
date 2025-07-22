package com.part4.team09.otboo.module.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResetPasswordRequest(

  @NotNull(message = "이메일 입력은 필수입니다.")
  @Email(message = "이메일 형식에 맞지 않습니다.")
  String email
) {

}
