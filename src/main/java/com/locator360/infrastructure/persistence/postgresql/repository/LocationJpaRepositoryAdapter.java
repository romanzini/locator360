package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSource;
import com.locator360.core.port.out.LocationRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.LocationJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LocationJpaRepositoryAdapter implements LocationRepository {

    private final SpringDataLocationRepository springDataLocationRepository;

    @Override
    public List<Location> saveAll(List<Location> locations) {
        log.debug("Saving {} location events", locations.size());
        List<LocationJpaEntity> entities = locations.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
        List<LocationJpaEntity> savedEntities = springDataLocationRepository.saveAll(entities);
        log.debug("Saved {} location events successfully", savedEntities.size());
        return savedEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Location> findByUserIdAndRecordedAtBetween(UUID userId, Instant start, Instant end) {
        log.debug("Finding locations for user: {} between {} and {}", userId, start, end);
        return springDataLocationRepository.findByUserIdAndRecordedAtBetween(userId, start, end).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Location toDomain(LocationJpaEntity entity) {
        return Location.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getCircleId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getAccuracyMeters(),
                entity.getSpeedMps(),
                entity.getHeadingDegrees(),
                entity.getAltitudeMeters(),
                LocationSource.valueOf(entity.getSource()),
                entity.getRecordedAt(),
                entity.getReceivedAt(),
                entity.isMoving(),
                entity.getBatteryLevel(),
                entity.getCreatedAt());
    }

    private LocationJpaEntity toJpaEntity(Location location) {
        LocationJpaEntity entity = new LocationJpaEntity();
        entity.setId(location.getId());
        entity.setUserId(location.getUserId());
        entity.setCircleId(location.getCircleId());
        entity.setLatitude(location.getLatitude());
        entity.setLongitude(location.getLongitude());
        entity.setAccuracyMeters(location.getAccuracyMeters());
        entity.setSpeedMps(location.getSpeedMps());
        entity.setHeadingDegrees(location.getHeadingDegrees());
        entity.setAltitudeMeters(location.getAltitudeMeters());
        entity.setSource(location.getSource().name());
        entity.setRecordedAt(location.getRecordedAt());
        entity.setReceivedAt(location.getReceivedAt());
        entity.setMoving(location.isMoving());
        entity.setBatteryLevel(location.getBatteryLevel());
        entity.setCreatedAt(location.getCreatedAt());
        return entity;
    }
}
