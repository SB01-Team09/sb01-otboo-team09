package com.part4.team09.otboo.module.domain.follow.repository;

import com.part4.team09.otboo.module.domain.follow.entity.Follow;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

}
