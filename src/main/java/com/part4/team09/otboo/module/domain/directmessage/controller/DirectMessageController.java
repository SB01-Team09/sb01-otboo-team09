package com.part4.team09.otboo.module.domain.directmessage.controller;

import com.part4.team09.otboo.module.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.part4.team09.otboo.module.domain.directmessage.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct-messages")
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    // DM 목록 조회
    @GetMapping
    public ResponseEntity<DirectMessageDtoCursorResponse> getDirectMessages(
            @RequestParam UUID userId,
            @RequestParam UUID loginUserId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(defaultValue = "10") int limit
            ){

        DirectMessageDtoCursorResponse response = directMessageService.getDirectMessages(userId, loginUserId, cursor, idAfter, limit);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
