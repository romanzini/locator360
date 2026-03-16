package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.PlaceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataPlaceRepository extends JpaRepository<PlaceJpaEntity, UUID> {

  List<PlaceJpaEntity> findByCircleId(UUID circleId);

  List<PlaceJpaEntity> findByCircleIdAndActiveTrue(UUID circleId);
}
