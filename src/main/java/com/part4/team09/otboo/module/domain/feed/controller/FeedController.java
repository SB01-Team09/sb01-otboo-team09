package com.part4.team09.otboo.module.domain.feed.controller;

import com.part4.team09.otboo.module.domain.feed.dto.CommentCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.CommentDto;
import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.service.CommentService;
import com.part4.team09.otboo.module.domain.feed.service.FeedService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;
  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<FeedDto> create(@RequestBody @Valid FeedCreateRequest request) {
    FeedDto feedDto = feedService.create(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(feedDto);
  }

  @PostMapping("/{feedId}/comments")
  public ResponseEntity<CommentDto> create(
      @PathVariable UUID feedId,
      @RequestBody @Valid CommentCreateRequest request
  ) {
    CommentDto commentDto = commentService.create(feedId, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(commentDto);
  }
}
