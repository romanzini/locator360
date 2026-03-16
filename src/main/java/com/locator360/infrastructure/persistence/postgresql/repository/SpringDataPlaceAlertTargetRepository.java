package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.PlaceAlertTargetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataPlaceAlertTargetRepository extends JpaRepository<PlaceAlertTargetJpaEntity, UUID> {

  List<PlaceAlertTargetJpaEntity> findByPolicyId(UUID policyId);

  void deleteByPolicyId(UUID policyId);
}
