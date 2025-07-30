package com.part4.team09.otboo.module.domain.notification.dto;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import java.util.List;
import java.util.UUID;

public record NotificationDtoCursorResponse(
    List<NotificationDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    int totalCount,
    String sortBy,
    SortDirection sortDirection
) {

}
