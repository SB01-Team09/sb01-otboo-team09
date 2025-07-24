package com.part4.team09.otboo.module.domain.notification.controller;

import com.part4.team09.otboo.module.domain.notification.dto.NotificationDtoCursorResponse;
import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<NotificationDtoCursorResponse> delete(
          @RequestParam(required = false) String cursor,
          @RequestParam(required = false) UUID idAfter,
          @RequestParam(defaultValue = "20") @Min(value = 1, message = "limit은 0보다 커야합니다.") int limit) {
    NotificationDtoCursorResponse response = notificationService.get(cursor, idAfter, limit);

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(@PathVariable UUID notificationId) {
    notificationService.delete(notificationId);

    return ResponseEntity
      .status(HttpStatus.NO_CONTENT)
      .build();
  }
}
