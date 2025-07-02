package com.part4.team09.otboo.module.domain.follow.contoller;

import com.part4.team09.otboo.module.domain.follow.dto.FollowCreateRequest;
import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListResponse;
import com.part4.team09.otboo.module.domain.follow.dto.FollowSummaryDto;
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

        FollowDto followDto = followService.create(request.followeeId(), request.followerId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(followDto);
    }

    // 팔로잉 목록 조회
    @GetMapping("/followings")
    public ResponseEntity<FollowListResponse> getFollowings(
            @RequestParam UUID followerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String nameLike
            ){

        FollowListResponse response = followService.getFollowings(followerId, cursor, idAfter, limit, nameLike);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 팔로워 목록 조회
    @GetMapping("/followers")
    public ResponseEntity<FollowListResponse> getFollowers(
            @RequestParam UUID followeeId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String nameLike
    ){

        FollowListResponse response = followService.getFollowers(followeeId, cursor, idAfter, limit, nameLike);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 팔로우 요악 정보 조회 TODO: loginUserId 파라미터 수정
    @GetMapping("/summary")
    public ResponseEntity<FollowSummaryDto> getFollowSummary(@RequestParam UUID userId, @RequestParam UUID loginUserId){

        FollowSummaryDto response = followService.getFollowSummary(userId, loginUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    // 팔로우 삭제
    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> Unfollow(@PathVariable UUID followId){
        followService.deleteFollow(followId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

}
