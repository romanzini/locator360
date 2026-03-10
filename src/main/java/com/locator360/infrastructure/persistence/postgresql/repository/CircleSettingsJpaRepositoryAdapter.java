package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.circle.CircleSettings;
import com.locator360.core.domain.circle.DrivingAlertLevel;
import com.locator360.core.port.out.CircleSettingsRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.CircleSettingsJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CircleSettingsJpaRepositoryAdapter implements CircleSettingsRepository {

    private final SpringDataCircleSettingsRepository springDataCircleSettingsRepository;

    @Override
    public CircleSettings save(CircleSettings settings) {
        log.debug("Saving circle settings for circleId: {}", settings.getCircleId());
        CircleSettingsJpaEntity entity = toJpaEntity(settings);
        CircleSettingsJpaEntity savedEntity = springDataCircleSettingsRepository.save(entity);
        log.debug("Circle settings saved successfully for circleId: {}", savedEntity.getCircleId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<CircleSettings> findByCircleId(UUID circleId) {
        log.debug("Finding circle settings by circleId: {}", circleId);
        return springDataCircleSettingsRepository.findByCircleId(circleId)
                .map(this::toDomain);
    }

    private CircleSettings toDomain(CircleSettingsJpaEntity entity) {
        return CircleSettings.restore(
                entity.getId(),
                entity.getCircleId(),
                DrivingAlertLevel.valueOf(entity.getDrivingAlertLevel()),
                entity.isAllowMemberChat(),
                entity.isAllowMemberSos(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private CircleSettingsJpaEntity toJpaEntity(CircleSettings settings) {
        CircleSettingsJpaEntity entity = new CircleSettingsJpaEntity();
        entity.setId(settings.getId());
        entity.setCircleId(settings.getCircleId());
        entity.setDrivingAlertLevel(settings.getDrivingAlertLevel().name());
        entity.setAllowMemberChat(settings.isAllowMemberChat());
        entity.setAllowMemberSos(settings.isAllowMemberSos());
        entity.setCreatedAt(settings.getCreatedAt());
        entity.setUpdatedAt(settings.getUpdatedAt());
        return entity;
    }
}
