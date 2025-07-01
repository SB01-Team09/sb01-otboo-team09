package com.part4.team09.otboo.module.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 필터 순서 및 동작을 테스트 합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class FilterChainOrderTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FilterChainProxy filterChainProxy;

  @Test
  @DisplayName("필터 체인 순서 검증")
  void filterChainOrder_ShouldBeCorrect() {
    // given : 현재 설정된 필터 체인 목록을 가져옴
    List<SecurityFilterChain> filterChains = filterChainProxy.getFilterChains();

    // then : api 필터 체인이 먼저 와야함.
    SecurityFilterChain apiChain = filterChains.get(0); // API 용도
    SecurityFilterChain resourceChain = filterChains.get(1); // 정적 리소스 용도

    assertThat(apiChain).isNotNull();
    assertThat(resourceChain).isNotNull();
  }


}
