package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;

    public FollowDto create(UUID followeeId, UUID followerId){
        // 직접 프로필 상세 들어가서 팔로우 걸기 (전체 사용자 검색 안됨) -> 예외 처리 필요 x
        // DB에 팔로워, 팔로이 추가하고, 나중에 팔로우 정보 요약에는 count
        log.debug("팔로우 생성 시작: follower={}, followee={}", followerId, followeeId);

        Follow follow = Follow.create(followeeId, followerId);
        Follow savedFollow = followRepository.save(follow);

        log.info("팔로우 저장 완료: id={}", savedFollow.getId());

        // 당한 사람한테 알림 발송

        return FollowDto.fromEntity(savedFollow);
    }

}
