package com.familylocator.core.port.out;

import com.familylocator.core.domain.user.AuthIdentity;
import com.familylocator.core.domain.user.AuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface AuthIdentityRepository {

  AuthIdentity save(AuthIdentity authIdentity);

  Optional<AuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider);

  Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
