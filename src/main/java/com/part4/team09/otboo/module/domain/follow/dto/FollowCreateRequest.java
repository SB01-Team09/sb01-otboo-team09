package com.part4.team09.otboo.module.domain.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FollowCreateRequest {
    private UUID followeeId;
    private UUID followerId;

}
