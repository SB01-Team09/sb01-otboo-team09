package com.part4.team09.otboo.module.common.security.oauth;

import com.part4.team09.otboo.module.common.security.oauth.entity.OAuthProvider;
import com.part4.team09.otboo.module.common.security.oauth.entity.OAuthProvider.SocialType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthProviderRepository extends JpaRepository<OAuthProvider, UUID> {

  Optional<OAuthProvider> findByProviderAndProviderId(SocialType provider, String providerId);
}
