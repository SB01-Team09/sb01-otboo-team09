package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListResponse;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.exception.FollowNotFoundException;
import com.part4.team09.otboo.module.domain.follow.exception.NegativeLimitNotAllowed;
import com.part4.team09.otboo.module.domain.follow.mapper.FollowMapper;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


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

    @Mock
    private FollowRepositoryQueryDSL followRepositoryQueryDSL;


    @Test
    @DisplayName("팔로우 등록 성공")
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

    @Test
    @DisplayName("팔로잉 목록 조회 성공")
    void searchFollowings() {
        // given
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();

        UUID idAfter = UUID.randomUUID(); // 커서 기준 ID
        LocalDateTime createdAtAfter = LocalDateTime.of(2025, 6, 30, 12, 0); // 커서 기준 시간
        int limit = 1;
        String nameLike = "연경";

        // follow2: 커서 기준이 되는 데이터
        Follow follow2 = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow2, "id", idAfter);
        ReflectionTestUtils.setField(follow2, "createdAt", createdAtAfter);

        // follow1: 커서 이후 데이터
        Follow follow1 = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(follow1, "createdAt", createdAtAfter.minusSeconds(1)); // createdAt이 더 과거여야함

        List<Follow> follows = List.of(follow1, follow2);

        // Mock 설정
        when(followRepository.findById(idAfter)).thenReturn(Optional.of(follow2));
        when(followRepositoryQueryDSL.countFollowings(followerId, nameLike)).thenReturn(2);
        when(followRepositoryQueryDSL.getFollowings(eq(followerId), eq(idAfter), eq(createdAtAfter), eq(nameLike), any()))
                .thenReturn(follows);

        when(followMapper.toDto(any())).thenReturn(
                new FollowDto(
                        follow1.getId(),
                        new UserSummary(followeeId, "followee", null),
                        new UserSummary(followerId, "follower", null)
                )
        );

        // when
        FollowListResponse result = followService.getFollowings(followerId, idAfter, limit, nameLike);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.data()).hasSize(1);
        assertThat(result.nextIdAfter()).isEqualTo(follow1.getId());
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공")
    void searchFollowers() {
        // given
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();

        UUID idAfter = UUID.randomUUID(); // 커서 기준 ID
        LocalDateTime createdAtAfter = LocalDateTime.of(2025, 6, 30, 12, 0); // 커서 기준 시간
        int limit = 1;
        String nameLike = "연경";

        // follow2: 커서 기준이 되는 데이터
        Follow follow2 = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow2, "id", idAfter);
        ReflectionTestUtils.setField(follow2, "createdAt", createdAtAfter);

        // follow1: 커서 이후 데이터
        Follow follow1 = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(follow1, "createdAt", createdAtAfter.minusSeconds(1)); // createdAt이 더 과거여야함

        List<Follow> follows = List.of(follow1, follow2);

        // Mock 설정
        when(followRepository.findById(idAfter)).thenReturn(Optional.of(follow2));
        when(followRepositoryQueryDSL.countFollowers(followeeId, nameLike)).thenReturn(2);
        when(followRepositoryQueryDSL.getFollowers(eq(followeeId), eq(idAfter), eq(createdAtAfter), eq(nameLike), any()))
                .thenReturn(follows);

        when(followMapper.toDto(any())).thenReturn(
                new FollowDto(
                        follow1.getId(),
                        new UserSummary(followeeId, "followee", null),
                        new UserSummary(followerId, "follower", null)
                )
        );

        // when
        FollowListResponse result = followService.getFollowers(followeeId, idAfter, limit, nameLike);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.data()).hasSize(1);
        assertThat(result.nextIdAfter()).isEqualTo(follow1.getId());
    }

    @Test
    @DisplayName("팔로우 목록 조회 실패: limit 0 이하 예외처리")
    void getAllFollowingsWithNegativeLimit() {
        // given
        UUID followerId = UUID.randomUUID();

        // when, then: 예외 처리 반환되도록
        assertThatThrownBy(() -> followService.getFollowings(followerId, null, 0, null))
                .isInstanceOf(NegativeLimitNotAllowed.class);
    }

    @Test
    @DisplayName("팔로우 삭제 성공")
    void unfollowSuccess() {
        // given
        UUID followId = UUID.randomUUID();

        // existsById가 true를 반환하도록 설정
        when(followRepository.existsById(followId)).thenReturn(true);

        // doNothing: void 메서드 mocking
        doNothing().when(followRepository).deleteById(followId);

        // when
        followService.deleteFollow(followId);

        // then
        verify(followRepository).existsById(followId);
        verify(followRepository).deleteById(followId);
    }

    @Test
    @DisplayName("팔로우 삭제 실패 - 존재하지 않는 팔로우 ID")
    void unfollowFail_NotFound() {
        // given
        UUID followId = UUID.randomUUID();

        // existsById가 false를 반환 → 예외 발생 조건
        when(followRepository.existsById(followId)).thenReturn(false);

        // when, then
        assertThrows(FollowNotFoundException.class, () -> {
            followService.deleteFollow(followId);
        });

        // deleteById는 호출되지 않아야 함
        verify(followRepository).existsById(followId);
        verify(followRepository, never()).deleteById(any());
    }

}