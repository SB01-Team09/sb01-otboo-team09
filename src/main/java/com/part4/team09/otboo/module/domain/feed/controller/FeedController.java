package com.part4.team09.otboo.module.domain.feed.controller;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDtoCursorResponse;
import com.part4.team09.otboo.module.domain.feed.dto.request.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.service.CommentService;
import com.part4.team09.otboo.module.domain.feed.service.FeedService;
import com.part4.team09.otboo.module.domain.feed.service.LikeService;
import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import com.part4.team09.otboo.module.domain.weather.entity.Weather;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;
  private final CommentService commentService;
  private final LikeService likeService;

  @PreAuthorize("principal.id == #request.authorId")
  @PostMapping
  public ResponseEntity<FeedDto> createFeed(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid FeedCreateRequest request
  ) {
    UUID userId = userDetails.getId();
    log.info("사용자 [{}] 피드 생성 요청", userId);

    FeedDto feedDto = feedService.create(userId, request);
    log.debug("피드 생성 완료 - feedId: {}", feedDto.id());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(feedDto);
  }

  @PatchMapping("/{feedId}")
  public ResponseEntity<FeedDto> updateFeed(
      @PathVariable UUID feedId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid FeedUpdateRequest request
  ) {
    UUID userId = userDetails.getId();
    log.info("사용자 [{}] 피드 수정 요청 - feedId: {}", userId, feedId);

    FeedDto feedDto = feedService.update(feedId, userId, request);
    log.debug("피드 수정 완료 - feedId: {}", feedDto.id());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(feedDto);
  }

  @DeleteMapping("/{feedId}")
  public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
    log.info("피드 삭제 요청 - feedId: {}", feedId);

    feedService.delete(feedId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  // 피드 목록 조회
  @GetMapping
  public ResponseEntity<FeedDtoCursorResponse> getFeeds(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(defaultValue = "20") @Min(value = 1, message = "limit은 0보다 커야합니다.") int limit,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESCENDING") SortDirection sortDirection,
      @RequestParam(required = false) String keywordLike,
      @RequestParam(required = false) Weather.SkyStatus skyStatusEqual,
      @RequestParam(required = false) Precipitation.PrecipitationType precipitationTypeEqual,
      @RequestParam(required = false) UUID authorIdEqual) {
    UUID userId = currentUser.getId();
    log.info("사용자 [{}] 피드 목록 조회 요청 - limit: {}, cursor: {}", userId, limit, cursor);

    FeedListRequest request = new FeedListRequest(cursor, idAfter, limit, sortBy, sortDirection,
        keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual);
    FeedDtoCursorResponse response = feedService.getFeeds(currentUser.getId(), request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

  @PreAuthorize("principal.id == #request.authorId")
  @PostMapping("/{feedId}/comments")
  public ResponseEntity<CommentDto> createComment(
      @PathVariable UUID feedId,
      @RequestBody @Valid CommentCreateRequest request
  ) {
    log.info("댓글 생성 요청 - feedId: {}", feedId);

    CommentDto commentDto = commentService.create(feedId, request);

    log.debug("댓글 생성 완료 - commentId: {}", commentDto.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(commentDto);
  }

  // 댓글 목록 조회
  @GetMapping("/{feedId}/comments")
  public ResponseEntity<CommentDtoCursorResponse> getComments(
      @RequestParam UUID feedId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(defaultValue = "10") int limit
  ) {
    log.info("댓글 목록 조회 요청 - feedId: {}, limit: {}", feedId, limit);

    CommentDtoCursorResponse response = commentService.getComments(feedId, cursor, idAfter, limit);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

  @PostMapping("/{feedId}/like")
  public ResponseEntity<FeedDto> createLike(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID feedId
  ) {
    UUID userId = userDetails.getId();
    log.info("좋아요 요청 - 사용자: {}, feedId: {}", userId, feedId);

    FeedDto feedDto = likeService.create(userId, feedId);

    log.debug("좋아요 반영 - 현재 좋아요 수: {}", feedDto.likeCount());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(feedDto);
  }

  @DeleteMapping("/{feedId}/like")
  public ResponseEntity<Void> deleteLike(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID feedId
  ) {
    UUID userId = userDetails.getId();
    log.info("좋아요 취소 요청 - 사용자: {}, feedId: {}", userId, feedId);

    likeService.delete(userId, feedId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
