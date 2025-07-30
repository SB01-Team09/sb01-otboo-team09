package com.part4.team09.otboo.module.domain.directmessage.controller;

import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team09.otboo.module.domain.directmessage.service.DirectMessageService;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct-messages")
public class DirectMessageController {

  private final DirectMessageService directMessageService;

  // DM 목록 조회
  @GetMapping
  public ResponseEntity<DirectMessageDtoCursorResponse> getDirectMessages(
      @RequestParam UUID userId,
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(defaultValue = "10") @Min(value = 1, message = "limit은 0보다 커야합니다.") int limit
  ) {
    log.info(
        "DirectMessage 조회 요청 - 요청자 userId: {}, 조회 대상 userId: {}, cursor: {}, idAfter: {}, limit: {}",
        currentUser != null ? currentUser.getId() : "익명", userId, cursor, idAfter, limit);

    DirectMessageDtoCursorResponse response = directMessageService.getDirectMessages(userId,
        currentUser, cursor, idAfter, limit);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }
}
