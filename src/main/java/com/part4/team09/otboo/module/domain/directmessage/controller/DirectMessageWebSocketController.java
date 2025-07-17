package com.part4.team09.otboo.module.domain.directmessage.controller;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DirectMessageWebSocketController {

  private final DirectMessageService directMessageService;

  @PreAuthorize("principal.id == #request.senderId")
  @MessageMapping("/direct-messages_send")
  public void send(@Payload DirectMessageCreateRequest request) {
    DirectMessageDto directMessageDto = directMessageService.create(request);
  }

}
