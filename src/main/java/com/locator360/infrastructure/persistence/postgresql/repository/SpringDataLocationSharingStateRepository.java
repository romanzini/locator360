package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.LocationSharingStateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataLocationSharingStateRepository extends JpaRepository<LocationSharingStateJpaEntity, UUID> {

    Optional<LocationSharingStateJpaEntity> findByUserIdAndCircleId(UUID userId, UUID circleId);
}
