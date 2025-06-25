package com.part4.team09.otboo.module.domain.follow.contoller;

import com.part4.team09.otboo.module.domain.follow.dto.FollowCreateRequest;
import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우 요청
    @PostMapping
    public ResponseEntity<FollowDto> follow(@RequestBody FollowCreateRequest request){
        log.info("팔로우 요청: followee={} -> follower={}", request.followeeId(), request.followerId());

        FollowDto followDto = followService.create(request.followeeId(), request.followerId());

        log.info("팔로우 완료: followId={}", followDto.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(followDto);
    }

}
