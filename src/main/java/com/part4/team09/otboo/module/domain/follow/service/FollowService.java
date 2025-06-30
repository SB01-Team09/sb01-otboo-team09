package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListResponse;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.exception.FollowNotFoundException;
import com.part4.team09.otboo.module.domain.follow.exception.NegativeLimitNotAllowed;
import com.part4.team09.otboo.module.domain.follow.exception.SelfFollowNotAllowedException;
import com.part4.team09.otboo.module.domain.follow.mapper.FollowMapper;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowRepositoryQueryDSL followRepositoryQueryDSL;
    private final FollowMapper followMapper;

    // 팔로우 등록
    public FollowDto create(UUID followeeId, UUID followerId){
        // 예외처리 1. existsById시 유저가 존재 x    2. 자기자신은 팔로우 불가
        if(!userRepository.existsById(followeeId)){
            throw new UserNotFoundException(followeeId);
        }
        if(followeeId.equals(followerId)){
            throw new SelfFollowNotAllowedException(followeeId);
        }

        log.info("팔로우 생성 시작: follower={}, followee={}", followerId, followeeId);

        Follow follow = Follow.create(followeeId, followerId);
        Follow savedFollow = followRepository.save(follow);

        log.info("팔로우 저장 완료: id={}", savedFollow.getId());

        // 팔로우 당한 사람(팔로이)한테 알림 발송


        return followMapper.toDto(savedFollow);
    }


    // 팔로잉 목록 조회
    public FollowListResponse getFollowings(UUID followerId, UUID idAfter, int limit, String nameLike){

        log.info("팔로잉 목록 조회 시작: followerId={}, idAfter={}, limit={}, nameLike={}", followerId, idAfter, limit, nameLike);

        // limit은 0보다 커야 한다는 예외처리
        if (limit <= 0) {
            throw new NegativeLimitNotAllowed(limit);
        }

        // JPQL 쿼리문에 페이징할 사이즈를 전달하기 위한 pageable 객체
        Pageable pageable = PageRequest.of(0, limit+1);

        List<Follow> pagedFollowList;
        LocalDateTime createdAtAfter = null;
        int totalCount;

        // createdAt을 기준으로 정렬하기 위해 현재 idAfter로 가장 마지막 값의 createdAt을 가져오기
        if(idAfter != null) {
            createdAtAfter = followRepository.findById(idAfter).orElseThrow().getCreatedAt();
        }

        // 팔로잉 목록 조회 시작
        totalCount = followRepositoryQueryDSL.countFollowings(followerId, nameLike);
        pagedFollowList = followRepositoryQueryDSL.getFollowings(followerId, idAfter, createdAtAfter, nameLike, pageable);

        log.debug("전체 팔로잉 카운트: {}", totalCount);


        // (limit+1)개만큼 가져온 이유는 hasNext를 계산하기 위함. (limit+1)개를 followList에 저장했기 때문에 사이즈가 limit보다 크다면 hasNext가 true인 것
        // hasNext 판단 후, 진짜 data는 (limit)개만큼 subList로 가져오기
        boolean hasNext = pagedFollowList.size() > limit;
        if(hasNext){
            pagedFollowList = pagedFollowList.subList(0, limit);
        }

        // 필로잉 목록 데이터 Dto 변환
        List<FollowDto> pagedFollowDtoList = pagedFollowList.stream()
                .map(followMapper::toDto)
                .collect(Collectors.toList());


        // 다음 커서 생성
        UUID nextIdAfter = null;

        if (hasNext && !pagedFollowList.isEmpty()) { // pagedFollowList NPE 방지하기
            nextIdAfter = pagedFollowList.get(limit - 1).getId();
        }

        String nextCursor = encodeIdAfterToCursor(nextIdAfter); // cursor는 idAfter을 Base64로 인코딩해서 만듭니다

        log.debug("hasNext: {}, 실제 반환 개수: {}", hasNext, pagedFollowList.size());
        log.debug("nextCursor: {}", nextCursor);
        log.info("팔로잉 목록 조회 끝");

        // 팔로잉, 팔로워 목록 조회에서는 클라이언트가 정렬 조건과 순서를 지정하지 않기 때문에 개발단에서 지정한 걸로 명시해서 반환 (id 기준, DESCENDING)
        return new FollowListResponse(pagedFollowDtoList, nextCursor, nextIdAfter, hasNext, totalCount,"createdAt, id", FollowListResponse.SortDirection.DESCENDING);
    }


    // 팔로워 목록 조회
    public FollowListResponse getFollowers(UUID followeeId, UUID idAfter, int limit, String nameLike){

        log.info("팔로워 목록 조회 시작: followeeId={}, idAfter={}, limit={}, nameLike={}", followeeId, idAfter, limit, nameLike);

        // limit 예외처리
        if (limit <= 0) {
            throw new NegativeLimitNotAllowed(limit);
        }

        // JPQL 쿼리문에 페이징할 사이즈를 전달하기 위한 pageable 객체
        Pageable pageable = PageRequest.of(0, limit+1);

        List<Follow> pagedFollowList;
        LocalDateTime createdAtAfter = null;
        int totalCount;

        // createdAt을 기준으로 정렬하기 위해 현재 idAfter로 가장 마지막 값의 createdAt을 가져오기
        if(idAfter != null) {
            createdAtAfter = followRepository.findById(idAfter).orElseThrow().getCreatedAt();
        }

        // 팔로워 목록 조회 시작
        totalCount = followRepositoryQueryDSL.countFollowers(followeeId, nameLike);
        pagedFollowList = followRepositoryQueryDSL.getFollowers(followeeId, idAfter, createdAtAfter, nameLike, pageable);

        log.debug("전체 팔로워 카운트: {}", totalCount);


        // hasNext 판단 후, 진짜 data는 (limit)개만큼 subList로 가져오기
        boolean hasNext = pagedFollowList.size() > limit;
        if(hasNext){
            pagedFollowList = pagedFollowList.subList(0, limit);
        }

        // 필로워 목록 데이터 Dto 변환
        List<FollowDto> pagedFollowDtoList = pagedFollowList.stream()
                .map(followMapper::toDto)
                .collect(Collectors.toList());


        // 다음 커서 생성
        UUID nextIdAfter = null;

        if (hasNext && !pagedFollowList.isEmpty()) { // pagedFollowList NPE 방지하기
            nextIdAfter = pagedFollowList.get(limit - 1).getId();
        }

        String nextCursor = encodeIdAfterToCursor(nextIdAfter); // cursor는 idAfter을 Base64로 인코딩해서 만듭니다

        log.debug("hasNext: {}, 실제 반환 개수: {}", hasNext, pagedFollowList.size());
        log.debug("nextCursor: {}", nextCursor);
        log.info("팔로워 목록 조회 끝");

        return new FollowListResponse(pagedFollowDtoList, nextCursor, nextIdAfter, hasNext, totalCount,"createdAt, id", FollowListResponse.SortDirection.DESCENDING);
    }

    // cursor 인코딩 로직
    private String encodeIdAfterToCursor(UUID idAfter){
        if (idAfter == null){
            return null;
        }
        String uuidStr = idAfter.toString();
        byte[] bytes = uuidStr.getBytes(StandardCharsets.UTF_8);
        String cursor = Base64.getEncoder().encodeToString(bytes);
        return cursor;
    }

    // 팔로우 삭제
    public void deleteFollow(UUID followId){
        // 해당 팔로우가 애초에 존재하지 않아서 취소할 수 없음 예외
        if(!followRepository.existsById(followId)){
            throw new FollowNotFoundException(followId);
        }
        followRepository.deleteById(followId);
    }
}
