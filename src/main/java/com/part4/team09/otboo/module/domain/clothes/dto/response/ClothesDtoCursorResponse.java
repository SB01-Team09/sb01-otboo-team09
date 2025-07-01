package com.part4.team09.otboo.module.domain.clothes.dto.response;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.clothes.dto.data.ClothesDto;
import java.util.List;
import java.util.UUID;

public record ClothesDtoCursorResponse(

    // 의상 정보
    List<ClothesDto> data,

    // 다음 커서
    String nextCursor,

    // 다음 요청의 보조 커서
    UUID nextIdAfter,

    // 다음 데이터가 있는지 여부
    boolean hasNext,

    // 총 데이터 개수
    int totalCount,

    // 정렬 기준
    String sortBy,

    // 정렬 방향
    SortDirection sortDirection

) {

}
