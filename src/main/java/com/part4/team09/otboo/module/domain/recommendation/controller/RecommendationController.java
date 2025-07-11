package com.part4.team09.otboo.module.domain.recommendation.controller;

import com.part4.team09.otboo.module.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

  private final RecommendationService recommendationService;

  @GetMapping
  public String getRecommendations() {
    return recommendationService.getRecommendations();
  }

}
