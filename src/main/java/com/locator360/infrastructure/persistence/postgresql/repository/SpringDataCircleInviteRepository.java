package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.CircleInviteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataCircleInviteRepository extends JpaRepository<CircleInviteJpaEntity, UUID> {

    Optional<CircleInviteJpaEntity> findByInviteCode(String inviteCode);

    List<CircleInviteJpaEntity> findByCircleId(UUID circleId);

    List<CircleInviteJpaEntity> findByCircleIdAndStatus(UUID circleId, String status);
}
