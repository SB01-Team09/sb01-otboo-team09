package com.part4.team09.otboo.module.domain.directmessage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.part4.team09.otboo.module.common.security.userdetails.CustomUserDetails;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageSendPayload;
import com.part4.team09.otboo.module.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.directmessage.mapper.DirectMessageDtoAssembler;
import com.part4.team09.otboo.module.domain.directmessage.repository.DirectMessageRepository;
import com.part4.team09.otboo.module.domain.directmessage.repository.DirectMessageRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.dto.UserSummary;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

  @Mock
  private DirectMessageRepository directMessageRepository;

  @Mock
  private DirectMessageRepositoryQueryDSL directMessageRepositoryQueryDSL;

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

  @Test
  @DisplayName("DM 목록 조회 성공")
  void getDirectMessagesSuccess() {
    // given
    UUID userId = UUID.randomUUID();
    UUID currentUserId = UUID.randomUUID();
    CustomUserDetails currentUser = mock(CustomUserDetails.class);
    when(currentUser.getId()).thenReturn(currentUserId);
    when(userRepository.existsById(userId)).thenReturn(true);

    // DirectMessage 엔티티 생성
    DirectMessage dm = DirectMessage.create(UUID.randomUUID(), UUID.randomUUID(), "안녕");
    ReflectionTestUtils.setField(dm, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(dm, "createdAt", LocalDateTime.now());

    // Repository에서 1개 메시지 리턴하도록 mocking
    when(directMessageRepositoryQueryDSL.getDirectMessages(any(), any(), any(), any(), anyInt()))
      .thenReturn(List.of(dm));
    when(directMessageRepositoryQueryDSL.countDirectMessages(any(), any())).thenReturn(1);

    // DTO 변환 mocking
    DirectMessageDto dto = new DirectMessageDto(
      (UUID) ReflectionTestUtils.getField(dm, "id"),
      (LocalDateTime) ReflectionTestUtils.getField(dm, "createdAt"),
      new UserSummary(dm.getSenderId(), "sender", null),
      new UserSummary(dm.getReceiverId(), "receiver", null),
      dm.getContent()
    );
    when(directMessageDtoAssembler.assemble(dm)).thenReturn(dto);

    // when
    DirectMessageDtoCursorResponse result = directMessageService.getDirectMessages(userId,
      currentUser, null, null, 10);

    // then
    assertThat(result).isNotNull();                     // 결과가 null 아니어야 함
    assertThat(result.data()).hasSize(1);       // 데이터 1개 리턴 확인
    assertThat(result.hasNext()).isFalse();             // 다음 페이지 없음을 확인
    assertThat(result.totalCount()).isEqualTo(1); // 총 메시지 개수 확인
  }
}