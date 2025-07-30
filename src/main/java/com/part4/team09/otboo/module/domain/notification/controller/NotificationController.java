package com.part4.team09.otboo.module.domain.notification.controller;

import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<NotificationDtoCursorResponse> get(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(defaultValue = "20") @Min(value = 1, message = "limit은 0보다 커야합니다.") int limit) {
    log.info("알림 목록 조회 요청 - 사용자ID: {}, cursor: {}, idAfter: {}, limit: {}",
        userDetails.getId(), cursor, idAfter, limit);

    NotificationDtoCursorResponse response = notificationService.get(userDetails.getId(), cursor,
        idAfter, limit);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(@PathVariable UUID notificationId) {
    log.info("알림 삭제 요청 - 알림ID: {}", notificationId);

    notificationService.delete(notificationId);

    log.info("알림 삭제 완료 - 알림ID: {}", notificationId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
