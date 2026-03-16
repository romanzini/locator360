package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.location.LocationSharingState;
import com.locator360.core.port.out.LocationSharingStateRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.LocationSharingStateJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LocationSharingStateJpaRepositoryAdapter implements LocationSharingStateRepository {

    private final SpringDataLocationSharingStateRepository springDataLocationSharingStateRepository;

    @Override
    public LocationSharingState save(LocationSharingState state) {
        log.debug("Saving location sharing state for user: {} in circle: {}", state.getUserId(), state.getCircleId());
        LocationSharingStateJpaEntity entity = toJpaEntity(state);
        LocationSharingStateJpaEntity savedEntity = springDataLocationSharingStateRepository.save(entity);
        log.debug("Location sharing state saved successfully for user: {} in circle: {}", state.getUserId(), state.getCircleId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<LocationSharingState> findByUserIdAndCircleId(UUID userId, UUID circleId) {
        log.debug("Finding location sharing state for user: {} in circle: {}", userId, circleId);
        return springDataLocationSharingStateRepository.findByUserIdAndCircleId(userId, circleId)
                .map(this::toDomain);
    }

    private LocationSharingState toDomain(LocationSharingStateJpaEntity entity) {
        return LocationSharingState.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getCircleId(),
                entity.isSharingLocation(),
                entity.isHistoryEnabled(),
                entity.getPausedUntil(),
                entity.getLastKnownLocationId(),
                entity.getLastUpdatedAt());
    }

    private LocationSharingStateJpaEntity toJpaEntity(LocationSharingState state) {
        LocationSharingStateJpaEntity entity = new LocationSharingStateJpaEntity();
        entity.setId(state.getId());
        entity.setUserId(state.getUserId());
        entity.setCircleId(state.getCircleId());
        entity.setSharingLocation(state.isSharingLocation());
        entity.setHistoryEnabled(state.isHistoryEnabled());
        entity.setPausedUntil(state.getPausedUntil());
        entity.setLastKnownLocationId(state.getLastKnownLocationId());
        entity.setLastUpdatedAt(state.getLastUpdatedAt());
        return entity;
    }
}
