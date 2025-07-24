package com.part4.team09.otboo.module.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

  @NotBlank(message = "이름은 비워둘 수 없습니다.")
  @Size(min = 2, message = "이름은 최소 2글자여야 합니다.")
  String name,

  @NotBlank(message = "이메일은 비워둘 수 없습니다.")
  @Email(message = "이메일 형식에 맞지 않습니다.")
  String email,

  @NotBlank(message = "비밀번호는 비워둘 수 없습니다.")
  @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다.")
  @Pattern(
    regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).+$",
    message = "비밀번호에는 최소 하나 이상의 특수문자를 포함해야 합니다."
  )
  String password

) {

}
