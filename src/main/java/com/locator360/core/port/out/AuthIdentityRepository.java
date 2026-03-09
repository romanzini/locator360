package com.locator360.core.port.out;

import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface AuthIdentityRepository {

  AuthIdentity save(AuthIdentity authIdentity);

  Optional<AuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider);

  Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
