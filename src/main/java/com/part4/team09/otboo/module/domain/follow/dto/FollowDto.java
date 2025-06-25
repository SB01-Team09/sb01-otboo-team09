package com.part4.team09.otboo.module.domain.follow.dto;

import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FollowDto {
    private UUID id;
    private UUID followeeId;
    private UUID followerId;

    public static FollowDto fromEntity(Follow followEntity) {
        return new FollowDto(followEntity.getId(), followEntity.getFolloweeId(), followEntity.getFollowerId());
    }
}
