package com.part4.team09.otboo.module.domain.recommendation.service;

import com.part4.team09.otboo.module.domain.recommendation.dto.response.RecommendationDto;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final RecommendationInteranlService recommendationInteranlService;

  public RecommendationDto getRecommendations(UUID weatherId, UUID userId) {
    CompletableFuture<RecommendationDto> llmFuture =
      recommendationInteranlService.getRecommendationsByLLM(weatherId, userId);
    CompletableFuture<RecommendationDto> fallbackFuture =
      recommendationInteranlService.getRecommendationsByCustomAlgorithm(weatherId, userId);

    try {
      // LLM 응답을 3초 내로 받으면 사용
      return llmFuture.get(3, TimeUnit.SECONDS);
    } catch (Exception e) {
      // 타임아웃 or 예외 시 fallback 사용
      log.info("custom algorithm response");
      return fallbackFuture.join();
    }
  }
}
