package com.part4.team09.otboo.module.domain.follow.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.follow.dto.FollowDto;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListRequest;
import com.part4.team09.otboo.module.domain.follow.dto.FollowListResponse;
import com.part4.team09.otboo.module.domain.follow.dto.FollowSummaryDto;
import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import com.part4.team09.otboo.module.domain.follow.exception.FollowNotFoundException;
import com.part4.team09.otboo.module.domain.follow.mapper.FollowMapper;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepositoryQueryDSL;
import com.part4.team09.otboo.module.domain.user.exception.UserNotFoundException;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowRepositoryQueryDSL followRepositoryQueryDSL;
    private final FollowMapper followMapper;

    // 팔로우 등록
    @Transactional
    public FollowDto create(UUID followeeId, UUID followerId) {
        // 예외처리 1. existsById시 유저가 존재 x    2. 자기자신은 팔로우 불가
        if (!userRepository.existsById(followeeId)) {
            throw UserNotFoundException.withId(followeeId);
        }
        if (followeeId.equals(followerId)) {
            throw UserNotFoundException.withId(followeeId);
        }

        log.info("팔로우 생성 시작: follower={}, followee={}", followerId, followeeId);

        Follow follow = Follow.create(followeeId, followerId);
        Follow savedFollow = followRepository.save(follow);

        log.info("팔로우 저장 완료: id={}", savedFollow.getId());

        // TODO: 팔로우 당한 사람(팔로이)한테 알림 발송

        return followMapper.toDto(savedFollow);
    }


    // 팔로잉 목록 조회
    @Transactional(readOnly = true)
    public FollowListResponse getFollowings(UUID followerId, String cursor, UUID idAfter, int limit,
                                            String nameLike) {

        log.info("팔로잉 목록 조회 시작: followerId={}, idAfter={}, limit={}, nameLike={}", followerId, idAfter, limit, nameLike);

        // cursor을 LocalDateTime으로 디코딩
        LocalDateTime decodedCursor = decodeCursor(cursor);

        // 팔로잉 목록 조회 시작
        FollowListRequest request = new FollowListRequest(followerId, decodedCursor, idAfter, limit+1, nameLike);
        List<Follow> pagedFollowList;
        int totalCount;

        totalCount = followRepositoryQueryDSL.countFollowings(followerId, nameLike);
        pagedFollowList = followRepositoryQueryDSL.getFollowings(request);

        log.debug("전체 팔로잉 카운트: {}", totalCount);

        // 쿼리에서 (limit+1)개만큼 가져온 이유는 hasNext를 계산하기 위함. (limit+1)개를 followList에 저장했기 때문에 사이즈가 limit보다 크다면 hasNext가 true인 것
        // hasNext 판단 후, 진짜 data는 (limit)개만큼 subList로 가져오기
        boolean hasNext = pagedFollowList.size() > limit;
        if (hasNext) {
            pagedFollowList = pagedFollowList.subList(0, limit);
        }

        // 필로잉 목록 데이터 Dto 변환
        List<FollowDto> pagedFollowDtoList = pagedFollowList.stream()
                .map(followMapper::toDto)
                .collect(Collectors.toList());

        // 다음 커서 생성
        LocalDateTime nextCursorBeforeEncoding = null;
        UUID nextIdAfter = null;
        if (hasNext && !pagedFollowList.isEmpty()) { // pagedFollowList NPE 방지하기
            nextCursorBeforeEncoding = pagedFollowList.get(limit - 1).getCreatedAt();
            nextIdAfter = pagedFollowList.get(limit - 1).getId();
        }

        // cursor을 String으로 인코딩
        String nextCursor = encodeCursor(nextCursorBeforeEncoding);

        log.info("팔로잉 목록 조회 끝");

        // 팔로잉, 팔로워 목록 조회에서는 클라이언트가 정렬 조건과 순서를 지정하지 않기 때문에 개발단에서 지정한 걸로 명시해서 반환 (id 기준, DESCENDING)
        return new FollowListResponse(pagedFollowDtoList, nextCursor, nextIdAfter, hasNext, totalCount,
                "createdAt, id", SortDirection.DESCENDING);
    }


    // 팔로워 목록 조회
    @Transactional(readOnly = true)
    public FollowListResponse getFollowers(UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike) {

        log.info("팔로워 목록 조회 시작: followeeId={}, idAfter={}, limit={}, nameLike={}", followeeId, idAfter, limit, nameLike);

        // cursor을 LocalDateTime으로 디코딩
        LocalDateTime decodedCursor = decodeCursor(cursor);

        // 팔로잉 목록 조회 시작
        FollowListRequest request = new FollowListRequest(followeeId, decodedCursor, idAfter, limit+1, nameLike);
        List<Follow> pagedFollowList;
        int totalCount;

        totalCount = followRepositoryQueryDSL.countFollowers(followeeId, nameLike);
        pagedFollowList = followRepositoryQueryDSL.getFollowers(request);

        log.debug("전체 팔로워 카운트: {}", totalCount);

        // 쿼리에서 (limit+1)개만큼 가져온 이유는 hasNext를 계산하기 위함. (limit+1)개를 followList에 저장했기 때문에 사이즈가 limit보다 크다면 hasNext가 true인 것
        // hasNext 판단 후, 진짜 data는 (limit)개만큼 subList로 가져오기
        boolean hasNext = pagedFollowList.size() > limit;
        if (hasNext) {
            pagedFollowList = pagedFollowList.subList(0, limit);
        }

        // 필로잉 목록 데이터 Dto 변환
        List<FollowDto> pagedFollowDtoList = pagedFollowList.stream()
                .map(followMapper::toDto)
                .collect(Collectors.toList());

        // 다음 커서 생성
        LocalDateTime nextCursorBeforeEncoding = null;
        UUID nextIdAfter = null;
        if (hasNext && !pagedFollowList.isEmpty()) { // pagedFollowList NPE 방지하기
            nextCursorBeforeEncoding = pagedFollowList.get(limit - 1).getCreatedAt();
            nextIdAfter = pagedFollowList.get(limit - 1).getId();
        }

        // cursor을 String으로 인코딩
        String nextCursor = encodeCursor(nextCursorBeforeEncoding);

        log.info("팔로워 목록 조회 끝");

        // 팔로잉, 팔로워 목록 조회에서는 클라이언트가 정렬 조건과 순서를 지정하지 않기 때문에 개발단에서 지정한 걸로 명시해서 반환 (id 기준, DESCENDING)
        return new FollowListResponse(pagedFollowDtoList, nextCursor, nextIdAfter, hasNext, totalCount,
                "createdAt, id", SortDirection.DESCENDING);
    }

    // cursor 인코딩 로직 (LocalDateTime -> String)
    private String encodeCursor(LocalDateTime cursor) {
        return cursor == null ? null : cursor.toString();
    }

    // cursor 디코딩 로직 (String -> LocalDateTime)
    private LocalDateTime decodeCursor(String cursor){
        return cursor == null || cursor.isEmpty() ? null : LocalDateTime.parse(cursor);
    }


    // 팔로우 요약 정보 조회 TODO: loginUserId 파라미터 수정
    @Transactional(readOnly = true)
    public FollowSummaryDto getFollowSummary(UUID userId, UUID loginUserId){
        // 두 유저가 존재하지 않을 경우 각각 예외 처리
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException.withId(userId);
        }

        if (!userRepository.existsById(loginUserId)) {
            throw UserNotFoundException.withId(loginUserId);
        }

        UUID followeeId = userId; // 조회 대상 userId
        int followerCount = followRepository.countFollowersForSummary(userId); // 조회 대상의 팔로워 수
        int followingCount = followRepository.countFollowingsForSummary(userId); // 조회 대상의 팔로잉 수
        boolean followedByMe = followRepository.followRelationship(userId, loginUserId); // 로그인한 사용자(Me, 팔로워)가 조회 대상(팔로이)를 팔로우하고 있는지 여부
        UUID followedByMeId = followRepository.followedByMeId(userId, loginUserId); // 로그인한 사용자(Me, 팔로워)가 조회 대상(팔로이)을 팔로우 했을 때의 팔로우 값 아이디
        boolean followingMe = followRepository.followRelationship(loginUserId, userId); // 조회 대상(팔로워)이 로그인한 사용자(Me, 팔로이)를 팔로우하고 있는지 여부

        return new FollowSummaryDto(followeeId, followerCount, followingCount, followedByMe, followedByMeId, followingMe);
    }


    // 팔로우 삭제
    @Transactional
    public void deleteFollow(UUID followId) {
        // 해당 팔로우가 애초에 존재하지 않아서 취소할 수 없음 예외
        if (!followRepository.existsById(followId)) {
            throw new FollowNotFoundException(followId);
        }
        followRepository.deleteById(followId);
    }
}