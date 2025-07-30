package com.part4.team09.otboo.module.domain.directmessage.controller;

import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageSendPayload;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.service.DirectMessageService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageWebSocketController {

  private final DirectMessageService directMessageService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/direct-messages_send")
  public void send(@Payload DirectMessageCreateRequest request, Principal principal) {
    log.info("DirectMessage 전송 요청 - senderId: {}, receiverId: {}, content 길이: {}",
        request.senderId(), request.receiverId(), request.content().length());

    validateSenderIsAuthenticatedUser(request.senderId(), principal);

    DirectMessageSendPayload directMessageSendPayload = directMessageService.create(request);

    String destination = "/sub/direct-messages_" + directMessageSendPayload.dmKey();
    messagingTemplate.convertAndSend(destination, directMessageSendPayload.directMessageDto());

    log.info("DirectMessage 전송 완료 - destination: {}, dmId: {}",
        destination, directMessageSendPayload.directMessageDto().id());
  }

  private void validateSenderIsAuthenticatedUser(UUID senderId, Principal principal) {
    if (!(principal instanceof UsernamePasswordAuthenticationToken authToken)) {
      log.warn("인증 정보 올바르지 않음 - principal: {}", principal);
      throw new IllegalStateException("인증 정보가 올바르지 않습니다.");
    }

    Object principalObj = authToken.getPrincipal();
    if (!(principalObj instanceof AuthUserDto authUser)) {
      log.warn("인증된 사용자 정보 없음 - principalObj: {}", principalObj);
      throw new IllegalStateException("인증된 사용자 정보가 없습니다.");
    }

    UUID userId = authUser.userId();
    if (!userId.equals(senderId)) {
      log.warn("본인 아닌 사용자 메시지 전송 시도 - 인증 userId: {}, 요청 senderId: {}", userId, senderId);
      throw new AccessDeniedException("본인만 메시지를 보낼 수 있습니다.");
    }
  }
}
