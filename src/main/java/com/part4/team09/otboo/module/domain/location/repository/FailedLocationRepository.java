package com.part4.team09.otboo.module.domain.location.repository;

import com.part4.team09.otboo.module.domain.weather.entity.FailedLocation;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedLocationRepository extends JpaRepository<FailedLocation, UUID> {

  boolean existsByRetryCountLessThanAndCreatedAtAfter(int retryCount, LocalDateTime createdAt);

  Optional<FailedLocation> findByFailedLocationIdAndRetryCountLessThanEqualAndCreatedAtAfter(
    String failedLocationId, int retryCount, LocalDateTime createdAt
  );
}
