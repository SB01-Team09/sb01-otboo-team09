package com.part4.team09.otboo.module.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.repository.CommentRepository;
import com.part4.team09.otboo.module.domain.feed.repository.FeedRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Disabled
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CommentServiceIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private FeedRepository feedRepository;
    @Autowired
    private UserRepository userRepository;

    private UUID feedId;
    private UUID authorId;

    @BeforeEach
    void setup() {
        // 1. 사용자 생성, 저장
        User author = User.createUser("yg@email.com", "연경", "password");
        userRepository.save(author);
        authorId = author.getId();

        // 2. 피드 생성, 저장
        UUID weatherId = UUID.randomUUID();
        Feed feed = Feed.create(authorId, weatherId, "피드 내용");
        feedRepository.save(feed);
        feedId = feed.getId();

        // 3. 댓글 여러개 생성
        for (int i = 1; i <= 5; i++) {
            Comment comment = Comment.create(feedId, authorId, "댓글 " + i);
            commentRepository.save(comment);
        }
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getCommentsSuccess() {
        // when
        CommentDtoCursorResponse response = commentService.getComments(feedId,null,null,3);

        // then
        assertThat(response.data()).hasSize(3);
        assertThat(response.totalCount()).isEqualTo(5);
        assertThat(response.hasNext()).isTrue(); // 총 5개 중 3개만 불러왔으니 다음 페이지 있음
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextIdAfter()).isNotNull();

        List<String> contents = response.data().stream().map(CommentDto::content).toList();

        assertThat(contents).containsExactly("댓글 1", "댓글 2", "댓글 3");
    }
}
