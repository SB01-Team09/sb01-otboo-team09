package com.part4.team09.otboo.module.domain.recommendation.controller;

import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.recommendation.dto.response.RecommendationDto;
import com.part4.team09.otboo.module.domain.recommendation.service.RecommendationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

  private final RecommendationService recommendationService;

  @GetMapping
  public ResponseEntity<RecommendationDto> getRecommendations(
    @RequestParam("weatherId") UUID weatherId,
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(recommendationService.getRecommendations(
      weatherId, userDetails.getId()
    ));
  }
}
