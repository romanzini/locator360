package com.familylocator.infrastructure.persistence.postgresql.repository;

import com.familylocator.core.domain.user.AuthIdentity;
import com.familylocator.core.domain.user.AuthProvider;
import com.familylocator.core.port.out.AuthIdentityRepository;
import com.familylocator.infrastructure.persistence.postgresql.entity.AuthIdentityJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AuthIdentityJpaRepositoryAdapter implements AuthIdentityRepository {

    private final SpringDataAuthIdentityRepository springDataAuthIdentityRepository;
    private final ModelMapper modelMapper;

    @Override
    public AuthIdentity save(AuthIdentity authIdentity) {
        log.debug("Saving auth identity for user: {}", authIdentity.getUserId());
        AuthIdentityJpaEntity entity = modelMapper.map(authIdentity, AuthIdentityJpaEntity.class);
        AuthIdentityJpaEntity savedEntity = springDataAuthIdentityRepository.save(entity);
        return modelMapper.map(savedEntity, AuthIdentity.class);
    }

    @Override
    public Optional<AuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider) {
        log.debug("Finding auth identity by userId: {} and provider: {}", userId, provider);
        return springDataAuthIdentityRepository
                .findByUserIdAndProvider(userId, provider.name())
                .map(entity -> modelMapper.map(entity, AuthIdentity.class));
    }

    @Override
    public Optional<AuthIdentity> findByProviderAndProviderUserId(
            AuthProvider provider, String providerUserId) {
        log.debug("Finding auth identity by provider: {} and providerUserId: {}",
                provider, providerUserId);
        return springDataAuthIdentityRepository
                .findByProviderAndProviderUserId(provider.name(), providerUserId)
                .map(entity -> modelMapper.map(entity, AuthIdentity.class));
    }
}
