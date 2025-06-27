package com.part4.team09.otboo.module.domain.follow.dto;

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
    public enum SortDirection {
        ASCENDING,
        DESCENDING
    }
}
