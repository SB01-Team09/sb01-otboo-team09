package com.part4.team09.otboo.module.domain.clothes.dto.request;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import java.util.UUID;

public record ClothesAttributeDefFindRequest(

    String cursor,
    UUID idAfter,
    int limit,
    String sortBy,
    SortDirection sortDirection,
    String keywordLike
) {

}
