package com.part4.team09.otboo.module.domain.user.repository;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.user.dto.request.UserListRequest;
import com.part4.team09.otboo.module.domain.user.entity.User;
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
class UserRepositoryQueryDSLTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRepositoryQueryDSL userRepositoryQueryDSL;

    @Test
    void getUsers_성공() {
        // given
        User user1 = User.createUser("user1@test.com", "유저1", "pass");
        User user2 = User.createAdmin("admin@test.com", "어드민", "pass");
        User user3 = User.createUser("user2@test.com", "유저2", "pass");
        user3.lock(); // 락 상태

        userRepository.saveAll(List.of(user1, user2, user3));

        UserListRequest request = new UserListRequest(
                null,
                null,
                10,
                "email",
                SortDirection.ASCENDING,
                "user",
                User.Role.USER,
                false
        );

        // when
        List<User> result = userRepositoryQueryDSL.getUsers(request);

        // then
        assertThat(result).hasSize(1); // user3는 locked=true 이므로 제외
        assertThat(result.get(0).getEmail()).isEqualTo("user1@test.com");
    }

    @Test
    void countUsers_성공() {
        // given
        User user1 = User.createUser("user1@test.com", "유저1", "pass");
        User user2 = User.createUser("user2@test.com", "유저2", "pass");
        user2.lock(); // 락 상태

        userRepository.saveAll(List.of(user1, user2));

        UserListRequest request = new UserListRequest(
                null,
                null,
                10,
                "email",
                SortDirection.ASCENDING,
                "user",
                User.Role.USER,
                false // locked = false만 세야 함
        );

        // when
        int count = userRepositoryQueryDSL.countUsers(request);

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getUsers_커서_정렬_성공() {
        // given
        User user1 = User.createUser("aaa@test.com", "유저1", "pass");
        User user2 = User.createUser("bbb@test.com", "유저2", "pass");
        User user3 = User.createUser("ccc@test.com", "유저3", "pass");

        userRepository.saveAll(List.of(user1, user2, user3));

        UserListRequest request = new UserListRequest(
                "bbb@test.com",
                user2.getId(),
                10,
                "email",
                SortDirection.ASCENDING,
                null,
                null,
                null
        );

        // when
        List<User> result = userRepositoryQueryDSL.getUsers(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("ccc@test.com");
    }
}
