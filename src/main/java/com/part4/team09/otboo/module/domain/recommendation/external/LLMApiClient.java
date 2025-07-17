package com.part4.team09.otboo.module.domain.recommendation.external;

import com.part4.team09.otboo.module.domain.recommendation.dto.request.ContentRequest;
import com.part4.team09.otboo.module.domain.recommendation.dto.request.ContentRequest.Content;
import com.part4.team09.otboo.module.domain.recommendation.dto.request.ContentRequest.Part;
import com.part4.team09.otboo.module.domain.recommendation.dto.response.GeminiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class LLMApiClient {

  @Qualifier("geminiRestClient")
  private final RestClient geminiRestClient;

  @Value("${GEMINI_API_KEY:dev-placeholder-key}")
  private String API_KEY;

  public String getInfo(String text) {
    Part part = new Part(text);
    Content content = new Content(List.of(part));
    ContentRequest contentRequest = new ContentRequest(List.of(content));
    GeminiResponse response = geminiRestClient.post()
      .header("x-goog-api-key", API_KEY)
      .body(contentRequest)
      .retrieve()
      .toEntity(GeminiResponse.class)
      .getBody();

    return response.candidates().get(0).content().parts().get(0).text();
  }

}
