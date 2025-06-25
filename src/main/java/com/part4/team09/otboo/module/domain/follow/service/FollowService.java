package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.exception.SelfFollowNotAllowedException;
import com.part4.team09.otboo.module.domain.follow.mapper.FollowMapper;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    public FollowDto create(UUID followeeId, UUID followerId){
        // 예외처리 1. existsById시 유저가 존재 x    2. 자기자신은 팔로우 불가
        if(!userRepository.existsById(followeeId)){
            throw new UserNotFoundException(followeeId);
        }
        if(followeeId == followerId){
            throw new SelfFollowNotAllowedException(followeeId);
        }


        log.debug("팔로우 생성 시작: follower={}, followee={}", followerId, followeeId);

        Follow follow = Follow.create(followeeId, followerId);
        Follow savedFollow = followRepository.save(follow);

        log.info("팔로우 저장 완료: id={}", savedFollow.getId());

        // 당한 사람한테 알림 발송

        return followMapper.toDto(savedFollow);
    }

}
