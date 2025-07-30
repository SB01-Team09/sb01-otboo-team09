package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.domain.feed.entity.Like;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface LikeRepository extends JpaRepository<Like, UUID> {

  int countByFeedId(UUID feedId);

  boolean existsByUserIdAndFeedId(UUID userId, UUID feedId);

  Optional<Like> findByUserIdAndFeedId(UUID userId, UUID feedId);

  @Modifying
  @Transactional
  void deleteAllByFeedId(@Param("feedId") UUID feedId);
}
