package com.part4.team09.otboo.module.domain.feed.repository;

import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OotdRepository extends JpaRepository<Ootd, UUID> {
}
