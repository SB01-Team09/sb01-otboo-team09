package com.part4.team09.otboo.module.domain.feed.dto;

import java.util.UUID;

public record AuthorDto(
    UUID userId,
    String name,
    String profileImageUrl
) {

}
