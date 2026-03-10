package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.CircleSettingsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataCircleSettingsRepository extends JpaRepository<CircleSettingsJpaEntity, UUID> {

    Optional<CircleSettingsJpaEntity> findByCircleId(UUID circleId);
}
