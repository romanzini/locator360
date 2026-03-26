package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.PlaceEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface SpringDataPlaceEventRepository extends JpaRepository<PlaceEventJpaEntity, UUID> {

    @Query("SELECT e FROM PlaceEventJpaEntity e WHERE e.placeId = :placeId AND e.userId = :userId ORDER BY e.occurredAt DESC LIMIT 1")
    Optional<PlaceEventJpaEntity> findLastByPlaceIdAndUserId(@Param("placeId") UUID placeId,
                                                              @Param("userId") UUID userId);
}
