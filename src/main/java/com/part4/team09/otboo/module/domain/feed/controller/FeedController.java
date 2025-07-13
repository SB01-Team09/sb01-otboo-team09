package com.part4.team09.otboo.module.domain.feed.controller;

import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.domain.feed.dto.request.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedUpdateRequest;
import com.part4.team09.otboo.module.domain.feed.service.CommentService;
import com.part4.team09.otboo.module.domain.feed.service.FeedService;
import com.part4.team09.otboo.module.domain.feed.service.LikeService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    FeedDto feedDto = feedService.create(userId, request);

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

  @PreAuthorize("principal.id == #request.authorId")
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

  @PostMapping("/{feedId}/like")
  public ResponseEntity<FeedDto> createLike(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID feedId
  ) {
    UUID userId = userDetails.getId();
    FeedDto feedDto = likeService.create(userId, feedId);

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
    likeService.delete(userId, feedId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
