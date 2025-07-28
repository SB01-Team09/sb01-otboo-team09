package com.part4.team09.otboo.module.domain.directmessage.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.part4.team09.otboo.module.domain.auth.dto.AuthUserDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageSendPayload;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.service.DirectMessageService;
import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.security.Principal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class DirectMessageWebSocketControllerTest {

  @Mock
  private DirectMessageService directMessageService;

  @Mock
  private SimpMessagingTemplate simpMessagingTemplate;

  @InjectMocks
  private DirectMessageWebSocketController directMessageWebSocketController;

  @Nested
  @DisplayName("디엠 전송")
  public class SendDMTest {

    @Test
    @DisplayName("디엠 전송 성공")
    void send_dm_success() {
      // given
      UUID receiverId = UUID.randomUUID();
      UUID senderId = UUID.randomUUID();
      String dmKey = "dmKey";

      DirectMessageCreateRequest request = new DirectMessageCreateRequest(receiverId, senderId, "content");
      DirectMessageDto directMessageDto = mock(DirectMessageDto.class);
      DirectMessageSendPayload payload = new DirectMessageSendPayload(dmKey, directMessageDto);

      Principal principal = mockPrincipalWithUserId(senderId);

      given(directMessageService.create(request)).willReturn(payload);

      // when
      directMessageWebSocketController.send(request, principal);

      // then
      verify(directMessageService).create(request);
      verify(simpMessagingTemplate).convertAndSend("/sub/direct-messages_" + dmKey, directMessageDto);
    }

    @Test
    @DisplayName("디엠 전송 실패 - 올바르지 않은 인증정보")
    void send_throwsIllegalStateException_whenPrincipalIsNotUsernamePasswordAuthenticationToken() {
      // given
      UUID receiverId = UUID.randomUUID();
      UUID senderId = UUID.randomUUID();
      DirectMessageCreateRequest request = new DirectMessageCreateRequest(receiverId, senderId, "content");

      // 인증 객체가 잘못된 타입
      Principal invalidPrincipal = () -> "someUser";

      // when & then
      assertThatThrownBy(() -> directMessageWebSocketController.send(request, invalidPrincipal))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("인증 정보가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("디엠 전송 실패 - 인증된 사용자 정보가 없음")
    void send_throwsIllegalStateException_whenPrincipalIsNotAuthUserDto() {
      // given
      UUID receiverId = UUID.randomUUID();
      UUID senderId = UUID.randomUUID();
      var request = new DirectMessageCreateRequest(receiverId, senderId, "content");

      // UsernamePasswordAuthenticationToken 안에 엉뚱한 principal 넣기
      Object notAuthUserDto = new Object(); // 기대하는 타입이 아님
      Principal invalidPrincipal = new UsernamePasswordAuthenticationToken(notAuthUserDto, null);

      // when & then
      assertThatThrownBy(() -> directMessageWebSocketController.send(request, invalidPrincipal))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("인증된 사용자 정보가 없습니다.");
    }

    @Test
    @DisplayName("디엠 전송 실패 - 본인이 보내지 않은 메시지")
    void send_throwsAccessDeniedException_whenUserIsNotSender() {
      // given
      UUID receiverId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID wrongSenderId = UUID.randomUUID();
      DirectMessageCreateRequest request = new DirectMessageCreateRequest(receiverId, wrongSenderId, "content");

      Principal principal = mockPrincipalWithUserId(userId);

      // when & then
      assertThatThrownBy(() -> directMessageWebSocketController.send(request, principal))
          .isInstanceOf(AccessDeniedException.class)
          .hasMessage("본인만 메시지를 보낼 수 있습니다.");
    }
  }

  private Principal mockPrincipalWithUserId(UUID userId) {
    AuthUserDto authUser = new AuthUserDto(userId, "email", "nickname", false, Role.USER);
    return new UsernamePasswordAuthenticationToken(authUser, null);
  }
}