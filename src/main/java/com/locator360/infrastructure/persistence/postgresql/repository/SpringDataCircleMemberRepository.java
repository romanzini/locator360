package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.CircleMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataCircleMemberRepository extends JpaRepository<CircleMemberJpaEntity, UUID> {

    Optional<CircleMemberJpaEntity> findByCircleIdAndUserId(UUID circleId, UUID userId);

    List<CircleMemberJpaEntity> findByCircleId(UUID circleId);

    List<CircleMemberJpaEntity> findByUserId(UUID userId);

    long countByCircleId(UUID circleId);

    List<CircleMemberJpaEntity> findByCircleIdAndStatus(UUID circleId, String status);
}
