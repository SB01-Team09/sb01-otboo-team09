package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowCreateRequest;
import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock
    private FollowRepository followRepository;


    @Test
    void createFollow() {
        // given
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();

        Follow follow = Follow.create(followeeId, followerId);

        // 테스트용 아이디 생성
        UUID fakeId = UUID.randomUUID();
        ReflectionTestUtils.setField(follow, "id", fakeId);

        when(followRepository.save(any(Follow.class))).thenReturn(follow);

        // when
        FollowDto result = followService.create(followeeId, followerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFolloweeId()).isEqualTo(followeeId);
        assertThat(result.getFollowerId()).isEqualTo(followerId);

        verify(followRepository).save(any(Follow.class)); // save 메서드가 호출되었는지 확인
    }

}