package com.part4.team09.otboo.module.domain.follow.mapper;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowMapper {

    private final UserRepository userRepository;

    public FollowDto toDto(Follow follow) {

        User followee = userRepository.findById(follow.getFolloweeId())
                .orElseThrow(() -> new UserNotFoundException(follow.getFolloweeId()));
        User follower = userRepository.findById(follow.getFollowerId())
                .orElseThrow(() -> new UserNotFoundException(follow.getFollowerId()));

        return new FollowDto(
                follow.getId(),
                new UserSummary(followee.getId(), followee.getName(), followee.getProfileImageUrl()),
                new UserSummary(follower.getId(), follower.getName(), follower.getProfileImageUrl())
        );
    }}
