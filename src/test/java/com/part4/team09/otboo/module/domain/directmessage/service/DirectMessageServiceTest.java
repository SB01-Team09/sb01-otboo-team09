package com.part4.team09.otboo.module.domain.directmessage.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageSendPayload;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.directmessage.mapper.DirectMessageDtoAssembler;
import com.part4.team09.otboo.module.domain.directmessage.repository.DirectMessageRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

  @Mock
  private DirectMessageRepository directMessageRepository;

  @Mock
  private DirectMessageDtoAssembler directMessageDtoAssembler;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private DirectMessageService directMessageService;

  @Nested
  @DisplayName("디엠 생성")
  public class CreateDirectMessageTest {

    @Test
    @DisplayName("디엠 생성 성공")
    void create_directMessage_success() {
      // given
      UUID senderId = UUID.randomUUID();
      UUID receiverId = UUID.randomUUID();
      User mockUser = mock(User.class);
      User mockUser2 = mock(User.class);
      DirectMessage mockDirectMessage = mock(DirectMessage.class);
      DirectMessageDto mockDirectMessageDto = mock(DirectMessageDto.class);

      DirectMessageCreateRequest request = new DirectMessageCreateRequest(
          senderId,
          receiverId,
          "content"
      );

      given(userRepository.findById(senderId)).willReturn(Optional.of(mockUser));
      given(userRepository.findById(receiverId)).willReturn(Optional.of(mockUser2));
      given(directMessageRepository.save(any())).willReturn(mockDirectMessage);
      given(directMessageDtoAssembler.assemble(mockDirectMessage)).willReturn(mockDirectMessageDto);

      // when
      DirectMessageSendPayload result = directMessageService.create(request);

      // then
      assertEquals(mockDirectMessageDto, result.directMessageDto());
    }
  }
}