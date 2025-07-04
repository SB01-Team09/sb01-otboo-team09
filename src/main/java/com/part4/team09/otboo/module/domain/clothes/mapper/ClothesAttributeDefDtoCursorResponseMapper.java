package com.part4.team09.otboo.module.domain.clothes.mapper;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClothesAttributeDefDtoCursorResponseMapper {

  public ClothesAttributeDefDtoCursorResponse toDto(List<ClothesAttributeDefDto> data,
      String nextCursor, UUID nextIdAfter, boolean hasNext, int totalCount, String sortBy, SortDirection sortDirection) {

    return new ClothesAttributeDefDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount, sortBy, sortDirection);
  }

}
