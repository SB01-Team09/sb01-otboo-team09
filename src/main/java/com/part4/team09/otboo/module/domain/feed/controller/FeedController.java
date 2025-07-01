package com.part4.team09.otboo.module.domain.feed.controller;

import com.part4.team09.otboo.module.domain.feed.dto.FeedCreateRequest;
import com.part4.team09.otboo.module.domain.feed.dto.FeedDto;
import com.part4.team09.otboo.module.domain.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;

  @PostMapping
  public ResponseEntity<FeedDto> create(FeedCreateRequest request) {
    FeedDto feedDto = feedService.create(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(feedDto);
  }
}
