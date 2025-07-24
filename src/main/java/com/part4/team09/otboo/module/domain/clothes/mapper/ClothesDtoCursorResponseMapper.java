package com.part4.team09.otboo.module.domain.clothes.mapper;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import com.part4.team09.otboo.module.domain.clothes.dto.response.ClothesDtoCursorResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClothesDtoCursorResponseMapper {

  public ClothesDtoCursorResponse toDto(List<ClothesDto> data, String nextCursor, UUID nextIdAfter,
      boolean hasNext, int totalCount, String sortBy, SortDirection sortDirection) {

    return new ClothesDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount, sortBy,
        sortDirection);
  }
}
