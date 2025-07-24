package com.part4.team09.otboo.module.common.security.oauth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthProviderRepository extends JpaRepository<OAuthProvider, UUID> {

  Optional<OAuthProvider> findByProviderAndProviderId(String provider, String providerId);
}
