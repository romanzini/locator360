package com.familylocator.infrastructure.persistence.postgresql.repository;

import com.familylocator.infrastructure.persistence.postgresql.entity.AuthIdentityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataAuthIdentityRepository extends JpaRepository<AuthIdentityJpaEntity, UUID> {

  Optional<AuthIdentityJpaEntity> findByUserIdAndProvider(UUID userId, String provider);

  Optional<AuthIdentityJpaEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
