package com.part4.team09.otboo.module.domain.follow.repository;

import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // 검색어 X : JPQL

    // 전체 팔로잉 목록 조회: idAfter가 null이면 처음부터 반환, null이 아니면 커서페이징대로 진행
    @Query("""
    SELECT f FROM Follow f
    WHERE f.followerId = :followerId
      AND (:idAfter IS NULL OR f.id < :idAfter)
    ORDER BY f.id DESC """)
    List<Follow> getAllFollowings(
            @Param("followerId") UUID followerId,
            @Param("idAfter") UUID idAfter,
            Pageable pageable
    );

    // 전체 팔로잉 목록 전체 개수 (totalCount)
    @Query("""
    SELECT COUNT(f) FROM Follow f
    WHERE f.followerId = :followerId """)
    int countAllFollowings(
            @Param("followerId") UUID followerId
    );
}
