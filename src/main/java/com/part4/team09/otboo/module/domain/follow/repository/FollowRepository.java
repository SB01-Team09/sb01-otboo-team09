package com.part4.team09.otboo.module.domain.follow.repository;

import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // 조회 대상의 팔로워 수
    @Query("SELECT count(f) FROM Follow f WHERE f.followeeId = :followeeId")
    int countFollowersForSummary(@Param("followeeId") UUID userId);

    // 조회 대상의 팔로잉 수
    @Query("SELECT count(f) FROM Follow f WHERE f.followerId = :followerId")
    int countFollowingsForSummary(@Param("followerId") UUID userId);

    // 팔로우 여부
    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.followeeId = :followeeId AND f.followerId = :followerId")
    boolean followRelationship(@Param("followeeId") UUID followeeId, @Param("followerId") UUID followerId);

    // 로그인한 사용자(Me, 팔로워)가 조회 대상(팔로이)을 팔로우 했을 때의 팔로우 값 아이디
    @Query("SELECT f.id FROM Follow f WHERE f.followeeId = :followeeId AND f.followerId = :followerId")
    UUID followedByMeId(@Param("followeeId") UUID userId, @Param("followerId") UUID loginUserId);

}
