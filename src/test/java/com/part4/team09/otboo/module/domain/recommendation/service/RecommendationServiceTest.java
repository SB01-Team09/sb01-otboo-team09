package com.part4.team09.otboo.module.domain.recommendation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.domain.recommendation.dto.response.RecommendationDto;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock
  private RecommendationInteranlService recommendationInteranlService;
  @InjectMocks
  private RecommendationService recommendationService;

  private UUID weatherId = UUID.randomUUID();
  private UUID userId = UUID.randomUUID();

  @Test
  void getRecommendations_shouldReturnLLMResult_ifLLMRespondsInTime() {
    // given
    RecommendationDto llmDto = new RecommendationDto(weatherId, userId, List.of());
    CompletableFuture<RecommendationDto> llmFuture = CompletableFuture.completedFuture(llmDto);
    CompletableFuture<RecommendationDto> fallbackFuture = new CompletableFuture<>();

    when(recommendationInteranlService.getRecommendationsByLLM(weatherId, userId))
      .thenReturn(llmFuture);
    when(recommendationInteranlService.getRecommendationsByCustomAlgorithm(weatherId, userId))
      .thenReturn(fallbackFuture);

    // when
    RecommendationDto result = recommendationService.getRecommendations(weatherId, userId);

    // then
    assertEquals(llmDto, result);
  }

  @Test
  void getRecommendations_shouldReturnFallback_ifLLMFails() {
    // given
    CompletableFuture<RecommendationDto> llmFuture = new CompletableFuture<>();
    llmFuture.completeExceptionally(new TimeoutException());

    RecommendationDto fallbackDto = new RecommendationDto(weatherId, userId, List.of());
    CompletableFuture<RecommendationDto> fallbackFuture = CompletableFuture.completedFuture(
      fallbackDto);

    when(recommendationInteranlService.getRecommendationsByLLM(weatherId, userId))
      .thenReturn(llmFuture);
    when(recommendationInteranlService.getRecommendationsByCustomAlgorithm(weatherId, userId))
      .thenReturn(fallbackFuture);

    // when
    RecommendationDto result = recommendationService.getRecommendations(weatherId, userId);

    // then
    assertEquals(fallbackDto, result);
  }
}
