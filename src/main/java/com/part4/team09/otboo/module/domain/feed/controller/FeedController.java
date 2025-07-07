package com.part4.team09.otboo.module.domain.feed.controller;

import com.part4.team09.otboo.module.domain.feed.dto.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.service.CommentService;
import com.part4.team09.otboo.module.domain.feed.service.FeedService;
import com.part4.team09.otboo.module.domain.feed.service.LikeService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;
  private final CommentService commentService;
  private final LikeService likeService;

  // TODO: userId @AuthenticationPrincipal로 변경
  @PostMapping
  public ResponseEntity<FeedDto> createFeed(
      @RequestParam UUID userId,
      @RequestBody @Valid FeedCreateRequest request
  ) {
    FeedDto feedDto = feedService.create(userId, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(feedDto);
  }

  // TODO: userId @AuthenticationPrincipal로 변경
  @PatchMapping("/{feedId}")
  public ResponseEntity<FeedDto> updateFeed(
      @PathVariable UUID feedId,
      @RequestParam UUID userId,
      @RequestBody @Valid FeedUpdateRequest request
  ) {
    FeedDto feedDto = feedService.update(feedId, userId, request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(feedDto);
  }

  @DeleteMapping("/{feedId}")
  public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
    feedService.delete(feedId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
  
  @PostMapping("/{feedId}/comments")
  public ResponseEntity<CommentDto> createComment(
      @PathVariable UUID feedId,
      @RequestBody @Valid CommentCreateRequest request
  ) {
    CommentDto commentDto = commentService.create(feedId, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(commentDto);
  }

  // TODO: @AuthenticationPrincipal로 변경
  @PostMapping("/{feedId}/like")
  public ResponseEntity<FeedDto> createLike(
      @RequestParam UUID userId,
      @PathVariable UUID feedId
  ) {
    FeedDto feedDto = likeService.create(userId, feedId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(feedDto);
  }
}
