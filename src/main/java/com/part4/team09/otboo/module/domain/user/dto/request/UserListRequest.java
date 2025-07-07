package com.part4.team09.otboo.module.domain.user.dto.request;

import com.part4.team09.otboo.module.common.enums.SortDirection;

import java.util.UUID;

public record UserListRequest(
        String cursor,
        UUID idAfter,
        int limit,
        String sortBy,
        SortDirection sortDirection,
        String emailLike,
        RoleType roleEqual,
        Boolean locked
) {
    public enum RoleType{
        USER, ADMIN
    }
}
