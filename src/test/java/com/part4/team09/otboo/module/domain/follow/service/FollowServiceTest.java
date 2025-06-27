package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListResponse;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private FollowRepositoryQueryDSL followRepositoryQueryDSL;


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

    @Test
    @DisplayName("검색어 없이 팔로잉 목록 조회 성공")
    void getAllFollowingsWithoutSearch() {
        // given
        UUID followerId = UUID.randomUUID();
        UUID idAfter = UUID.randomUUID();
        int limit = 1;

        Follow follow = Follow.create(UUID.randomUUID(), followerId);
        UUID followId = UUID.randomUUID();
        ReflectionTestUtils.setField(follow, "id", followId);
        List<Follow> follows = List.of(follow, Follow.create(UUID.randomUUID(), followerId));

        // 전체 개수 5로 지정해줌
        when(followRepository.countAllFollowings(followerId)).thenReturn(5);
        // 팔로우 목록 반환하도록 지정
        when(followRepository.getAllFollowings(eq(followerId), eq(idAfter), any())).thenReturn(follows);
        // Dto 변환로직 지정
        when(followMapper.toDto(any())).thenReturn(new FollowDto(followId,
                new UserSummary(UUID.randomUUID(), "followee", null),
                new UserSummary(followerId, "follower", null)));

        // when
        FollowListResponse result = followService.getFollowings(followerId, idAfter, limit, null);

        // then
        assertThat(result.data()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("검색어 포함 팔로잉 목록 조회 성공")
    void searchFollowings() {
        // given
        UUID followerId = UUID.randomUUID();
        UUID idAfter = UUID.randomUUID();
        int limit = 1;
        String nameLike = "연경";

        Follow follow = Follow.create(UUID.randomUUID(), followerId);
        UUID followId = UUID.randomUUID();
        ReflectionTestUtils.setField(follow, "id", followId);
        List<Follow> follows = List.of(follow, Follow.create(UUID.randomUUID(), followerId));

        // 검색 개수 1로 지정해줌
        when(followRepositoryQueryDSL.countSearchedFollowings(followerId, nameLike)).thenReturn(1);
        // 팔로우 목록 반환하도록 지정
        when(followRepositoryQueryDSL.searchFollowings(eq(followerId), eq(idAfter), eq(nameLike), any())).thenReturn(follows);
        // Dto 변환로직 지정
        when(followMapper.toDto(any())).thenReturn(new FollowDto(followId,
                new UserSummary(UUID.randomUUID(), "followee", null),
                new UserSummary(followerId, "follower", null)));

        // when
        FollowListResponse result = followService.getFollowings(followerId, idAfter, limit, nameLike);

        // then
        assertThat(result.data()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("limit 0 이하 예외")
    void getAllFollowingsWithNegativeLimit() {
        // given
        UUID followerId = UUID.randomUUID();

        // when, then: 예외 처리 반환되도록
        assertThatThrownBy(() -> followService.getFollowings(followerId, null, 0, null))
                .isInstanceOf(NegativeLimitNotAllowed.class);
    }

}