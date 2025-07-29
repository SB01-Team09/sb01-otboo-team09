package com.part4.team09.otboo.module.domain.notification.sse;

import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.auth.exception.AuthenticationRequiredException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

  private final SseService sseService;

  @GetMapping
  public SseEmitter connect(@AuthenticationPrincipal CustomUserDetails userDetails) {

    if (userDetails == null) {
      throw AuthenticationRequiredException.noDetail();
    }

    UUID userId = userDetails.getId();

    return sseService.connect(userId);
  }
}
