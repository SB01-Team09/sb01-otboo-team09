package com.part4.team09.otboo.module.domain.follow.contoller;

import com.part4.team09.otboo.module.domain.follow.dto.FollowCreateRequest;
import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;

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

    // TODO: 팔로잉 리스트 조회
    @GetMapping("/api/follows/followings")
    public ResponseEntity<FollowDto> getFollowingList(
            @RequestParam UUID followerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam Integer limit,
            @RequestParam(required = false) String nameLike
            ){


    }

}
