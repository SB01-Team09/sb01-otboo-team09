package com.part4.team09.otboo.module.domain.directmessage.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.common.security.CustomUserDetails;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDto;
import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import com.part4.team09.otboo.module.domain.directmessage.mapper.DirectMessageMapper;
import com.part4.team09.otboo.module.domain.directmessage.repository.DirectMessageRepository;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final UserRepository userRepository;
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageMapper directMessageMapper;


    // DM 목록 조회
    public DirectMessageDtoCursorResponse getDirectMessages(UUID userId, @AuthenticationPrincipal CustomUserDetails currentUser, String cursor, UUID idAfter, int limit){
        // 예외처리
        if(!userRepository.existsById(userId)){
            throw UserNotFoundException.withId(userId);
        }

        // 쿼리
        // cursor을 LocalDateTime으로 디코딩
        LocalDateTime decodedCursor = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, limit+1);
        List<DirectMessage> directMessages = directMessageRepository.getDirectMessages(userId, currentUser.getId(), decodedCursor, idAfter, pageable);
        int totalCount = directMessageRepository.countDirectMessages(userId, currentUser.getId());

        // Dto 리스트로 변환
        List<DirectMessageDto> directMessageDtos = directMessages.stream().map(directMessageMapper::toDto).toList();

        // 반환
        // hasNext
        boolean hasNext = directMessageDtos.size() > limit;
        if (hasNext) {
            directMessageDtos = directMessageDtos.subList(0, limit);
        }

        // nextCursor, nextIdAfter
        LocalDateTime nextCursor = null;
        UUID nextIdAfter = null;
        if(hasNext && !directMessageDtos.isEmpty()){
            nextCursor = directMessageDtos.get(limit-1).createdAt();
            nextIdAfter = directMessageDtos.get(limit-1).id();
        }

        // nextCursor 인코딩
        String encodedNextCursor = encodeCursor(nextCursor);

        // 최종 반환
        return new DirectMessageDtoCursorResponse(directMessageDtos, encodedNextCursor, nextIdAfter, hasNext, totalCount, "createdAt, id", SortDirection.ASCENDING);

    }



    // cursor 인코딩 로직 (LocalDateTime -> String)
    private String encodeCursor(LocalDateTime cursor) {
        return cursor == null ? null : cursor.toString();
    }

    // cursor 디코딩 로직 (String -> LocalDateTime)
    private LocalDateTime decodeCursor(String cursor){
        return cursor == null || cursor.isEmpty() ? null : LocalDateTime.parse(cursor);
    }
}
