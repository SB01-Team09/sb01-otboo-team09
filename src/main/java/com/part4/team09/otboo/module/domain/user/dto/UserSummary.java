package com.part4.team09.otboo.module.domain.user.dto;

import java.util.UUID;

public record UserSummary(
        UUID userId,
        String name,
        String profileImageUrl
) {
}


