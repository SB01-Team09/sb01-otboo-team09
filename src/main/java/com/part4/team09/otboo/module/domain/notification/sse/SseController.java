package com.part4.team09.otboo.module.domain.notification.sse;

import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.auth.exception.AuthenticationRequiredException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

  private final SseService sseService;

  @GetMapping
  public SseEmitter connect(@AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      log.warn("인증 정보 없음 - SSE 연결 거부");
      throw AuthenticationRequiredException.noDetail();
    }

    UUID userId = userDetails.getId();
    log.info("SSE 연결 요청 수신 - userId: {}", userId);

    SseEmitter emitter = sseService.connect(userId);

    log.info("SSE 연결 성공 - userId: {}", userId);
    return emitter;
  }
}
