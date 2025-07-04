package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OotdRepository extends JpaRepository<Ootd, UUID> {

  @Query("SELECT o.clothesId FROM Ootd o WHERE o.feedId = :feedId")
  List<UUID> findClothesIdsByFeedId(@Param("feedId") UUID feedId);
}
