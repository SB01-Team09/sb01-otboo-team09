package com.part4.team09.otboo.module.domain.auth.repository;

import com.part4.team09.otboo.module.domain.auth.entity.UserTempPassword;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTempPasswordRepository extends JpaRepository<UserTempPassword, UUID> {

  Optional<UserTempPassword> findByUserId(UUID userId);

  void deleteByUserId(UUID uuid);
}
