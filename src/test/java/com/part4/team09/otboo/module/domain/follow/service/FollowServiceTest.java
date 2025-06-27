package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.mapper.FollowMapper;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowMapper followMapper;


    @Test
    @DisplayName("팔로우 등록 성공 케이스")
    void createFollow() {
        // given
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        Follow follow = Follow.create(followeeId, followerId);
        // 테스트용 아이디 생성
        UUID fakeId = UUID.randomUUID();
        ReflectionTestUtils.setField(follow, "id", fakeId);
        String name = "옷부";

        UserSummary followee = new UserSummary(followeeId, name, null);
        UserSummary follower = new UserSummary(followerId, name, null);
        FollowDto dto = new FollowDto(fakeId, followee, follower);


        when(userRepository.existsById(any(UUID.class))).thenReturn(true); // 유저가 정상적으로 존재할 때를 가정해줌
        when(followRepository.save(any(Follow.class))).thenReturn(follow);
        when(followMapper.toDto(any(Follow.class))).thenReturn(dto);

        // when
        FollowDto result = followService.create(followeeId, followerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.followee().userId()).isEqualTo(followeeId);
        assertThat(result.follower().userId()).isEqualTo(followerId);

        verify(followRepository).save(any(Follow.class)); // save 메서드가 호출되었는지 확인
    }

}