package com.part4.team09.otboo.module.domain.follow.repository;

import com.part4.team09.otboo.module.domain.follow.entity.Follow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // 팔로잉 목록 조회: 처음부터
    List<Follow> findAllByFollowerIdOrderByIdAtDesc(UUID followerId, Pageable pageable);

    // 팔로잉 목록 조회: idAfter부터
    List<Follow> findAllByFollowerIdAndIdLessThenOrderByIdAtDesc(UUID followerId, UUID idAfter, Pageable pageable);
}
