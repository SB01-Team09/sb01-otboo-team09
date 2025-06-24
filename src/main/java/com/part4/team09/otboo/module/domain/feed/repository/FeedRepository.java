package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FeedRepository extends JpaRepository<Feed, UUID> {

}
