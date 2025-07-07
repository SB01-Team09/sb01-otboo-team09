package com.part4.team09.otboo.module.common.security.jwt;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

  Optional<AuthToken> findByUserId(UUID userId);
}
