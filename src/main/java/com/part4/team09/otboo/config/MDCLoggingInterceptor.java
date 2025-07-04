package com.part4.team09.otboo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class MDCLoggingInterceptor implements HandlerInterceptor {

  public static final String REQUEST_ID = "requestId";
  public static final String REQUEST_METHOD = "requestMethod";
  public static final String REQUEST_URI = "requestUri";

  public static final String REQUEST_ID_HEADER = "Discodeit-Request-ID";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // 요청 ID 생성 (UUID)
    String requestId = UUID.randomUUID().toString().replaceAll("-", "");

    // MDC에 컨텍스트 정보 추가
    MDC.put(REQUEST_ID, requestId);
    MDC.put(REQUEST_METHOD, request.getMethod());
    MDC.put(REQUEST_URI, request.getRequestURI());

    // 응답 헤더에 요청 ID 추가
    response.setHeader(REQUEST_ID_HEADER, requestId);

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
