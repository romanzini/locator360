package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.PlaceAlertPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataPlaceAlertPolicyRepository extends JpaRepository<PlaceAlertPolicyJpaEntity, UUID> {

  Optional<PlaceAlertPolicyJpaEntity> findByPlaceId(UUID placeId);

  void deleteByPlaceId(UUID placeId);
}
