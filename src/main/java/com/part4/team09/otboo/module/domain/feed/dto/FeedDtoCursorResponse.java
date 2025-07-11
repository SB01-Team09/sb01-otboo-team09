package com.part4.team09.otboo.module.domain.feed.dto;

import java.util.List;
import java.util.UUID;

public record FeedDtoCursorResponse(
        List<FeedDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        int totalCount,
        String sortBy,
        String sortDirection
) {
}
