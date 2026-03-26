package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.place.PlaceEvent;
import com.locator360.core.domain.place.PlaceEventType;
import com.locator360.core.port.out.PlaceEventRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.PlaceEventJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PlaceEventJpaRepositoryAdapter implements PlaceEventRepository {

    private final SpringDataPlaceEventRepository springDataPlaceEventRepository;

    @Override
    public PlaceEvent save(PlaceEvent event) {
        log.debug("Persisting place event: {}", event.getId());
        PlaceEventJpaEntity entity = toJpaEntity(event);
        PlaceEventJpaEntity savedEntity = springDataPlaceEventRepository.save(entity);
        log.debug("Place event persisted successfully: {}", savedEntity.getId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<PlaceEvent> findLastByPlaceIdAndUserId(UUID placeId, UUID userId) {
        log.debug("Finding last place event for placeId: {} and userId: {}", placeId, userId);
        return springDataPlaceEventRepository.findLastByPlaceIdAndUserId(placeId, userId)
                .map(this::toDomain);
    }

    private PlaceEvent toDomain(PlaceEventJpaEntity entity) {
        return PlaceEvent.restore(
                entity.getId(),
                entity.getPlaceId(),
                entity.getCircleId(),
                entity.getUserId(),
                PlaceEventType.valueOf(entity.getEventType()),
                entity.getLocationId(),
                entity.getOccurredAt(),
                entity.getCreatedAt());
    }

    private PlaceEventJpaEntity toJpaEntity(PlaceEvent event) {
        PlaceEventJpaEntity entity = new PlaceEventJpaEntity();
        entity.setId(event.getId());
        entity.setPlaceId(event.getPlaceId());
        entity.setCircleId(event.getCircleId());
        entity.setUserId(event.getUserId());
        entity.setEventType(event.getEventType().name());
        entity.setLocationId(event.getLocationId());
        entity.setOccurredAt(event.getOccurredAt());
        entity.setCreatedAt(event.getCreatedAt());
        return entity;
    }
}
