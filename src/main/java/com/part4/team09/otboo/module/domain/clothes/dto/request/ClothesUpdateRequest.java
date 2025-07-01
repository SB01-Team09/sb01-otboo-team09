package com.part4.team09.otboo.module.domain.clothes.dto.request;

import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDto;
import com.part4.team09.otboo.module.domain.clothes.entity.Clothes.ClothesType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ClothesUpdateRequest(

  // 의상 이름
  @NotBlank(message = "의상 이름은 필수입니다.")
  @Size(max = 50, message = "의상 이름은 50자 이하여야 합니다.")
  String name,

  // 의상 타입
  @NotNull(message = "의상 타입은 필수입니다.")
  ClothesType type,

  // 의상 속성
  @Valid
  @NotNull(message = "속성 리스트는 null일 수 없습니다.")
  List<ClothesAttributeDto> attributes
) {

}
