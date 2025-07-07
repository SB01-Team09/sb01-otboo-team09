package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.domain.feed.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

  @Modifying
  @Transactional
  @Query("DELETE FROM Comment c WHERE c.feedId = :feedId")
  void deleteAllByFeedId(@Param("feedId") UUID feedId);
}
