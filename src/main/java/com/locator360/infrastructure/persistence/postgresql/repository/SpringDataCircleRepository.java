package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.CircleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataCircleRepository extends JpaRepository<CircleJpaEntity, UUID> {

    List<CircleJpaEntity> findByCreatedByUserId(UUID userId);
}
