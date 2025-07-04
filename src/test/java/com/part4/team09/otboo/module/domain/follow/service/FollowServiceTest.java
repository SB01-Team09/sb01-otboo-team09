package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListRequest;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListResponse;
import com.part4.team09.otboo.module.domain.follow.dto.FollowSummaryDto;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.event.FollowCacheEvictListener;
import com.part4.team09.otboo.module.domain.follow.event.FollowCreatedEvent;
import com.part4.team09.otboo.module.domain.follow.event.FollowDeletedEvent;
import com.part4.team09.otboo.module.domain.follow.exception.FollowNotFoundException;
import com.part4.team09.otboo.module.domain.follow.mapper.FollowMapper;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FollowCacheEvictListener followCacheEvictListener;


    @Test
    @DisplayName("팔로우 등록 성공")
    void createFollowSuccess() {
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
        doNothing().when(eventPublisher).publishEvent(any(FollowCreatedEvent.class));

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
    void getFollowingsSuccess() {
        // given
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();

        UUID idAfter = UUID.randomUUID();
        LocalDateTime cursor = LocalDateTime.of(2025, 6, 30, 0, 30, 0);
        int limit = 1;
        String nameLike = "연경";

        // follow2: 커서 기준 (더 최근)
        Follow follow2 = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow2, "id", idAfter);
        ReflectionTestUtils.setField(follow2, "createdAt", cursor);

        // follow1: 커서보다 더 과거
        Follow follow1 = Follow.create(followeeId, followerId);
        UUID id1 = UUID.randomUUID();
        LocalDateTime time1 = cursor.minusSeconds(1);
        ReflectionTestUtils.setField(follow1, "id", id1);
        ReflectionTestUtils.setField(follow1, "createdAt", time1);

        List<Follow> follows = List.of(follow1, follow2); // (limit + 1)개

        // Mock 설정
        when(followRepositoryQueryDSL.countFollowings(followerId, nameLike)).thenReturn(2);
        when(followRepositoryQueryDSL.getFollowings(any(FollowListRequest.class))).thenReturn(follows);

        when(followMapper.toDto(follow1)).thenReturn(
                new FollowDto(
                        id1,
                        new UserSummary(followeeId, "followee", null),
                        new UserSummary(followerId, "follower", null)
                )
        );

        // when
        String encodedCursor = cursor.toString(); // encodeCursor 방식과 동일
        FollowListResponse result = followService.getFollowings(followerId, encodedCursor, idAfter, limit, nameLike);

        // then
        assertThat(result.hasNext()).isTrue(); // limit + 1 이니까 true
        assertThat(result.data()).hasSize(1); // limit 만큼만 응답
        assertThat(result.nextIdAfter()).isEqualTo(id1); // follow1의 idAfter
        assertThat(result.nextCursor()).isEqualTo(time1.toString()); // follow1의 createdAt
    }


    @Test
    @DisplayName("팔로워 목록 조회 성공")
    void getFollowersSuccess() {
        // given
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();

        UUID idAfter = UUID.randomUUID();
        LocalDateTime cursor = LocalDateTime.of(2025, 6, 30, 12, 0);
        int limit = 1;
        String nameLike = "연경";

        // follow2: 커서 기준 (더 최근)
        Follow follow2 = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow2, "id", idAfter);
        ReflectionTestUtils.setField(follow2, "createdAt", cursor);

        // follow1: 커서보다 더 과거
        Follow follow1 = Follow.create(followeeId, followerId);
        UUID id1 = UUID.randomUUID();
        LocalDateTime time1 = cursor.minusSeconds(1);
        ReflectionTestUtils.setField(follow1, "id", id1);
        ReflectionTestUtils.setField(follow1, "createdAt", time1);

        List<Follow> follows = List.of(follow1, follow2); // (limit + 1)개

        // Mock 설정
        when(followRepositoryQueryDSL.countFollowers(followeeId, nameLike)).thenReturn(2);
        when(followRepositoryQueryDSL.getFollowers(any(FollowListRequest.class))).thenReturn(follows);

        when(followMapper.toDto(follow1)).thenReturn(
                new FollowDto(
                        follow1.getId(),
                        new UserSummary(followeeId, "followee", null),
                        new UserSummary(followerId, "follower", null)
                )
        );

        // when
        String encodedCursor = cursor.toString(); // encodeCursor 방식과 동일
        FollowListResponse result = followService.getFollowers(followeeId, encodedCursor, idAfter, limit, nameLike);

        // then
        assertThat(result.hasNext()).isTrue(); // limit + 1 이니까 true
        assertThat(result.data()).hasSize(1); // limit 만큼만 응답
        assertThat(result.nextIdAfter()).isEqualTo(follow1.getId()); // follow1의 idAfter
        assertThat(result.nextCursor()).isEqualTo(time1.toString()); // follow1의 createdAt
    }

    @Test
    @DisplayName("팔로우 요약 정보 조회 성공")
    void getFollowSummarySuccess() {
        // given
        UUID userId = UUID.randomUUID();        // 조회 대상
        UUID loginUserId = UUID.randomUUID();   // 로그인한 유저 (Me)
        UUID followedByMeId = null;      // 팔로우 관계 아이디

        int followerCount = 10;
        int followingCount = 5;
        boolean followedByMe = false;
        boolean followingMe = true;

        // Mock 설정
        // 유저 존재 여부
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(loginUserId)).thenReturn(true);

        when(followRepository.countFollowersForSummary(userId)).thenReturn(followerCount);
        when(followRepository.countFollowingsForSummary(userId)).thenReturn(followingCount);
        when(followRepository.followRelationship(userId, loginUserId)).thenReturn(followedByMe);
        when(followRepository.followedByMeId(userId, loginUserId)).thenReturn(followedByMeId);
        when(followRepository.followRelationship(loginUserId, userId)).thenReturn(followingMe);

        // when
        FollowSummaryDto result = followService.getFollowSummary(userId, loginUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.followeeId()).isEqualTo(userId);
        assertThat(result.followerCount()).isEqualTo(followerCount);
        assertThat(result.followingCount()).isEqualTo(followingCount);
        assertThat(result.followedByMe()).isEqualTo(followedByMe);
        assertThat(result.followedByMeId()).isEqualTo(followedByMeId);
        assertThat(result.followingMe()).isEqualTo(followingMe);
    }


    @Test
    @DisplayName("팔로우 요약 정보 조회 실패: 조회 대상 유저 Not Found")
    void getFollowSummaryFail_UserNotFound() {
        // given
        UUID userId = UUID.randomUUID();        // 조회 대상
        UUID loginUserId = UUID.randomUUID();   // 로그인한 유저 (Me)

        // Mock 설정
        // 유저 존재 여부 false로 설정
        when(userRepository.existsById(userId)).thenReturn(false);

        // when, then
        assertThrows(UserNotFoundException.class, () -> {
            followService.getFollowSummary(userId, loginUserId);
        });
    }


    @Test
    @DisplayName("팔로우 요약 정보 조회 실패: 로그인 유저 Not Found")
    void getFollowSummaryFail_LoginUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();        // 조회 대상
        UUID loginUserId = UUID.randomUUID();   // 로그인한 유저 (Me)

        // Mock 설정
        // 유저 존재 여부 false로 설정
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(loginUserId)).thenReturn(false);

        // when, then
        assertThrows(UserNotFoundException.class, () -> {
            followService.getFollowSummary(userId, loginUserId);
        });
    }



    @Test
    @DisplayName("팔로우 삭제 성공")
    void unfollowSuccess() {
        // given
        UUID followId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();

        Follow follow = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow, "id", followId);

        // existsById가 true를 반환하도록 설정
        when(followRepository.existsById(followId)).thenReturn(true);
        when(followRepository.findById(followId)).thenReturn(Optional.of(follow));
        doNothing().when(followRepository).deleteById(followId);
        doNothing().when(eventPublisher).publishEvent(any(FollowDeletedEvent.class));

        // when
        followService.deleteFollow(followId);

        // 이벤트 수동 호출
        followCacheEvictListener.handle(new FollowDeletedEvent(followeeId, followerId));

        // then
        verify(followRepository).existsById(followId);
        verify(followRepository).deleteById(followId);
    }


    @Test
    @DisplayName("팔로우 삭제 실패 - 존재하지 않는 팔로우 ID")
    void unfollowFail_NotFound() {
        // given
        UUID followId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();

        Follow follow = Follow.create(followeeId, followerId);
        ReflectionTestUtils.setField(follow, "id", followId);

        // existsById가 false를 반환 → 예외 발생 조건
        when(followRepository.existsById(followId)).thenReturn(false);
        when(followRepository.findById(followId)).thenReturn(Optional.of(follow));

        // when, then
        assertThrows(FollowNotFoundException.class, () -> {
            followService.deleteFollow(followId);
        });

        // deleteById는 호출되지 않아야 함
        verify(followRepository).existsById(followId);
        verify(followRepository, never()).deleteById(any());
    }

}