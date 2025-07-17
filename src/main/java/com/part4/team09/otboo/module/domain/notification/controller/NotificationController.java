package com.part4.team09.otboo.module.domain.notification.controller;

import com.part4.team09.otboo.module.domain.notification.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(@PathVariable UUID notificationId) {
    notificationService.delete(notificationId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
