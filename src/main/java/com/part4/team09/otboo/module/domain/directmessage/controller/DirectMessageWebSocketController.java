package com.part4.team09.otboo.module.domain.directmessage.controller;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageSendPayload;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DirectMessageWebSocketController {

  private final DirectMessageService directMessageService;
  private final SimpMessagingTemplate messagingTemplate;

//  @PreAuthorize("authentication.principal.id == #request.senderId")
  @MessageMapping("/direct-messages_send")
  public void send(@Payload DirectMessageCreateRequest request) {
    DirectMessageSendPayload directMessageSendPayload = directMessageService.create(request);

    String destination = "/sub/direct-messages_" + directMessageSendPayload.dmKey();
    messagingTemplate.convertAndSend(destination, directMessageSendPayload.directMessageDto());
  }
}
