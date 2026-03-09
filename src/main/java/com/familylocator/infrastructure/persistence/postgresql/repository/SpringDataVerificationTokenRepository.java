package com.familylocator.infrastructure.persistence.postgresql.repository;

import com.familylocator.infrastructure.persistence.postgresql.entity.VerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataVerificationTokenRepository extends JpaRepository<VerificationTokenJpaEntity, UUID> {

    Optional<VerificationTokenJpaEntity> findByToken(String token);

    Optional<VerificationTokenJpaEntity> findByUserIdAndType(UUID userId, String type);
}
