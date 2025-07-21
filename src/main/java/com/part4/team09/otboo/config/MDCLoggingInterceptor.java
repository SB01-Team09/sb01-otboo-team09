package com.part4.team09.otboo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class MDCLoggingInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // 요청 ID 생성 (UUID)
    String requestId = UUID.randomUUID().toString().replaceAll("-", "");

    // MDC에 컨텍스트 정보 추가
    MDC.put("requestId", requestId);
    MDC.put("requestMethod", request.getMethod());
    MDC.put("requestUrl", request.getRequestURI());
    String clientIp = request.getHeader("X-Forwarded-For");
    if (clientIp == null || clientIp.isBlank()) {
      clientIp = request.getRemoteAddr();
    }
    MDC.put("clientIp", clientIp);

    // 응답 헤더에 요청 ID 추가
    response.setHeader("Otboo-Request-ID", requestId);

    log.debug("Request started");
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    // 요청 처리 후 MDC 데이터 정리
    log.debug("Request completed");
    MDC.clear();
  }
}
