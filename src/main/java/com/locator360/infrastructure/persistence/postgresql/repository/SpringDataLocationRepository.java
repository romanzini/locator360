package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.LocationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface SpringDataLocationRepository extends JpaRepository<LocationJpaEntity, UUID> {

    List<LocationJpaEntity> findByUserIdAndRecordedAtBetween(UUID userId, Instant start, Instant end);
}
