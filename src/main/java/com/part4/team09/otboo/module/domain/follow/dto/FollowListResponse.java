package com.part4.team09.otboo.module.domain.follow.dto;

import com.part4.team09.otboo.module.common.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public record FollowListResponse(
        List<FollowDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        Integer totalCount,
        String sortBy,
        SortDirection sortDirection

) {

}
