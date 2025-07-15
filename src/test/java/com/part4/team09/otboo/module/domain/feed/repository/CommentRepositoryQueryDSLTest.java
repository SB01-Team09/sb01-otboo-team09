package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CommentRepositoryQueryDSLTest {

    @Autowired
    private CommentRepositoryQueryDSL commentRepositoryQueryDSL;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID feedId;

    @BeforeEach
    void setUp() {
        User user = User.createUser("은수@email.com", "은수", "otboo1!");
        userRepository.save(user);

        Feed feed = Feed.create(UUID.randomUUID(), UUID.randomUUID(), "피드입니다");
        feedRepository.save(feed);

        this.feedId = feed.getId();

        for (int i = 0; i < 5; i++) {
            Comment comment = Comment.create(feedId, user.getId(), "댓글 " + i);
            commentRepository.save(comment);
        }
    }

    @Test
    void getComments_전체조회_성공() {
        // when
        List<Comment> comments = commentRepositoryQueryDSL.getComments(feedId, null, null, 10);

        // then
        assertThat(comments).hasSize(5);
        assertThat(comments.get(0).getContent()).isEqualTo("댓글 0");
        assertThat(comments.get(4).getContent()).isEqualTo("댓글 4");
    }

    @Disabled
    @Test
    void getComments_커서조회_성공() {
        // given
        List<Comment> all = commentRepositoryQueryDSL.getComments(feedId, null, null, 10);
        Comment cursorComment = all.get(2);
        String cursor = cursorComment.getCreatedAt().toString();
        UUID idAfter = cursorComment.getId();

        // when
        List<Comment> result = commentRepositoryQueryDSL.getComments(feedId, cursor, idAfter, 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("댓글 3");
        assertThat(result.get(1).getContent()).isEqualTo("댓글 4");
    }

    @Test
    void countComments_성공() {
        // when
        int count = commentRepositoryQueryDSL.countComments(feedId);

        // then
        assertThat(count).isEqualTo(5);
    }
}