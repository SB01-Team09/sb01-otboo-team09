package com.part4.team09.otboo.module.domain.clothes.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ClothesAttributeDefCreateRequest(

  // 속성 정의 이름
  @NotBlank(message = "속성 정의 이름은 필수입니다.")
  @Size(max = 50, message = "속성 정의 이름은 50자 이하여야 합니다.")
  String name,

  // 선택 가능한 값 목록
  @NotEmpty(message = "선택 가능한 값은 1개 이상 있어야 합니다.")
  @Valid
  List<@NotBlank(message = "값 이름은 필수입니다.")
  @Size(max = 50, message = "값 이름은 50자 이하여야 합니다.")
    String> selectableValues
) {

}
