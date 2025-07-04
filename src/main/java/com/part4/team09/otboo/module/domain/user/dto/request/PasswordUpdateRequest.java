package com.part4.team09.otboo.module.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(

  @NotBlank(message = "변경할 비밀번호를 입력해주세요.")
  @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다.")
  @Pattern(
    regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).+$",
    message = "비밀번호에는 최소 하나 이상의 특수문자를 포함해야 합니다."
  )
  String password
) {

}
