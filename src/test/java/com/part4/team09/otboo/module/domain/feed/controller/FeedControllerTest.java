package com.part4.team09.otboo.module.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part4.team09.otboo.module.domain.feed.dto.AuthorDto;
import com.part4.team09.otboo.module.domain.feed.dto.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.OotdDto;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.service.CommentService;
import com.part4.team09.otboo.module.domain.feed.service.FeedService;
import com.part4.team09.otboo.module.domain.feed.service.LikeService;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
@WebMvcTest(FeedController.class)
class FeedControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private FeedService feedService;

  @MockitoBean
  private CommentService commentService;

  @MockitoBean
  private LikeService likeService;

  @Nested
  @DisplayName("피드 생성")
  public class CreateFeedTest {

    @Test
    @DisplayName("피드 생성 성공")
    void create_feed_success() throws Exception {
      // given
      UUID feedId = UUID.randomUUID();
      Weather mockWeather = mock(Weather.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);
      List<OotdDto> ootdDtos = List.of();

      FeedCreateRequest request = new FeedCreateRequest(
          UUID.randomUUID(),
          UUID.randomUUID(),
          List.of(UUID.randomUUID()),
          "content"
      );

      FeedDto feedDto = new FeedDto(
          feedId,
          LocalDateTime.now(),
          LocalDateTime.now(),
          mockAuthorDto,
          mockWeather,
          ootdDtos,
          "content",
          0,
          0,
          false
      );

      given(feedService.create(any(FeedCreateRequest.class))).willReturn(feedDto);

      // when & then
      mockMvc.perform(post("/api/feeds")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(feedId.toString()))
          .andExpect(jsonPath("$.content").value("content"));
    }
  }

  @Nested
  @DisplayName("댓글 생성")
  public class CreateCommentTest {

    @Test
    @DisplayName("댓글 생성 성공")
    void create_comment_success() throws Exception {
      // given
      UUID feedId = UUID.randomUUID();
      AuthorDto mockAuthorDto = mock(AuthorDto.class);

      CommentCreateRequest request = new CommentCreateRequest(
          feedId,
          UUID.randomUUID(),
          "content"
      );

      CommentDto commentDto = new CommentDto(
          UUID.randomUUID(),
          LocalDateTime.now(),
          feedId,
          mockAuthorDto,
          "content"
      );

      given(commentService.create(feedId, request)).willReturn(commentDto);

      // when & then
      mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("content"));
    }
  }

  @Nested
  @DisplayName("좋아요 생성")
  public class CreateLikeTest {

    @Test
    @DisplayName("좋아요 생성 성공")
    void create_like_success() throws Exception {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Weather mockWeather = mock(Weather.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);
      List<OotdDto> ootdDtos = List.of();

      FeedDto feedDto = new FeedDto(
          feedId,
          LocalDateTime.now(),
          LocalDateTime.now(),
          mockAuthorDto,
          mockWeather,
          ootdDtos,
          "content",
          0,
          0,
          false
      );

      given(likeService.create(userId, feedId)).willReturn(feedDto);

      // when & then
      mockMvc.perform(post("/api/feeds/{feedId}/like", feedId)
              .contentType(MediaType.APPLICATION_JSON)
              .param("userId", userId.toString())
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(feedId.toString()))
          .andExpect(jsonPath("$.content").value("content"));
    }
  }

  @Nested
  @DisplayName("피드 수정")
  public class UpdateFeedTest {

    @Test
    @DisplayName("피드 수정 성공")
    void update_feed_success() throws Exception {
      // given
      UUID feedId = UUID.randomUUID();
      Weather mockWeather = mock(Weather.class);
      AuthorDto mockAuthorDto = mock(AuthorDto.class);
      List<OotdDto> ootdDtos = List.of();

      FeedUpdateRequest request = new FeedUpdateRequest("newContent");

      FeedDto feedDto = new FeedDto(
          feedId,
          LocalDateTime.now(),
          LocalDateTime.now(),
          mockAuthorDto,
          mockWeather,
          ootdDtos,
          "newContent",
          0,
          0,
          false
      );

      given(feedService.update(feedId, request)).willReturn(feedDto);

      // when & then
      mockMvc.perform(patch("/api/feeds/{feedId}", feedId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(feedId.toString()))
          .andExpect(jsonPath("$.content").value("newContent"));
    }
  }
}
