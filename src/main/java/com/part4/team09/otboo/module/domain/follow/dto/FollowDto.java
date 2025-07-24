package com.part4.team09.otboo.module.domain.follow.dto;

import com.part4.team09.otboo.module.domain.user.dto.UserSummary;

import java.util.UUID;


public record FollowDto(
        UUID id,
        UserSummary followee,
        UserSummary follower
) {

}