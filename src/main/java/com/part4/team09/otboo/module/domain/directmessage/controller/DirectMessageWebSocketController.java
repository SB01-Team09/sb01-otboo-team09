package com.part4.team09.otboo.module.domain.directmessage.controller;

import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageSendPayload;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.service.DirectMessageService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DirectMessageWebSocketController {

  private final DirectMessageService directMessageService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/direct-messages_send")
  public void send(@Payload DirectMessageCreateRequest request, Principal principal) {
    validateSenderIsAuthenticatedUser(request.senderId(), principal);

    DirectMessageSendPayload directMessageSendPayload = directMessageService.create(request);

    String destination = "/sub/direct-messages_" + directMessageSendPayload.dmKey();
    messagingTemplate.convertAndSend(destination, directMessageSendPayload.directMessageDto());
  }

  private void validateSenderIsAuthenticatedUser(UUID senderId, Principal principal) {
    if (!(principal instanceof UsernamePasswordAuthenticationToken authToken)) {
      throw new IllegalStateException("인증 정보가 올바르지 않습니다.");
    }

    Object principalObj = authToken.getPrincipal();
    if (!(principalObj instanceof AuthUserDto authUser)) {
      throw new IllegalStateException("인증된 사용자 정보가 없습니다.");
    }

    UUID userId = authUser.userId();
    if (!userId.equals(senderId)) {
      throw new AccessDeniedException("본인만 메시지를 보낼 수 있습니다.");
    }
  }
}
