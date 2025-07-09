package com.part4.team09.otboo.module.domain.follow.repository;

import com.part4.team09.otboo.module.domain.follow.dto.FollowListRequest;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class FollowRepositoryQueryDSLTest {

    @Autowired
    private FollowRepositoryQueryDSL followRepositoryQueryDSL;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getFollowings_성공() {
        // given
        // 유저 저장: 쿼리에서 user 조인을 하기 때문.
        User followee = User.createUser("followee@email.com", "팔로이", "password");
        userRepository.save(followee);
        User follower = User.createUser("follower@email.com", "팔로워", "password");
        userRepository.save(follower);

        // 팔로우 저장
        Follow follow = Follow.create(followee.getId(), follower.getId());
        followRepository.save(follow);

        FollowListRequest request = new FollowListRequest(
                follower.getId(),
                null,
                null,
                10,
                "팔로이"
        );

        // when
        List<Follow> result = followRepositoryQueryDSL.getFollowings(request);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFolloweeId()).isEqualTo(followee.getId());
    }

    @Test
    void getFollowers_성공() {
        // given
        // 유저 저장: 쿼리에서 user 조인을 하기 때문.
        User followee = User.createUser("followee@email.com", "팔로이", "password");
        userRepository.save(followee);
        User follower = User.createUser("follower@email.com", "팔로워", "password");
        userRepository.save(follower);

        // 팔로우 저장
        Follow follow = Follow.create(followee.getId(), follower.getId());
        followRepository.save(follow);

        FollowListRequest request = new FollowListRequest(
                followee.getId(),
                null,
                null,
                10,
                "팔로워"
        );

        // when
        List<Follow> result = followRepositoryQueryDSL.getFollowers(request);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFollowerId()).isEqualTo(follower.getId());
    }

    @Test
    void countFollowings_성공() {
        // given
        // 유저 저장: 쿼리에서 user 조인을 하기 때문.
        User followee = User.createUser("followee@email.com", "팔로이", "password");
        userRepository.save(followee);
        User follower = User.createUser("follower@email.com", "팔로워", "password");
        userRepository.save(follower);

        // 팔로우 저장
        Follow follow = Follow.create(followee.getId(), follower.getId());
        followRepository.save(follow);

        // when
        int count = followRepositoryQueryDSL.countFollowings(follower.getId(), "");

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countFollowers_성공() {
        // given
        // 유저 저장: 쿼리에서 user 조인을 하기 때문.
        User followee = User.createUser("followee@email.com", "팔로이", "password");
        userRepository.save(followee);
        User follower = User.createUser("follower@email.com", "팔로워", "password");
        userRepository.save(follower);

        // 팔로우 저장
        Follow follow = Follow.create(followee.getId(), follower.getId());
        followRepository.save(follow);

        // when
        int count = followRepositoryQueryDSL.countFollowers(followee.getId(), "");

        // then
        assertThat(count).isEqualTo(1);
    }

}