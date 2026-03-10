package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.circle.CircleSettings;
import com.locator360.core.port.out.CircleSettingsRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.CircleSettingsJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CircleSettingsJpaRepositoryAdapter implements CircleSettingsRepository {

    private final SpringDataCircleSettingsRepository springDataCircleSettingsRepository;
    private final ModelMapper modelMapper;

    @Override
    public CircleSettings save(CircleSettings settings) {
        log.debug("Saving circle settings for circleId: {}", settings.getCircleId());
        CircleSettingsJpaEntity entity = modelMapper.map(settings, CircleSettingsJpaEntity.class);
        CircleSettingsJpaEntity savedEntity = springDataCircleSettingsRepository.save(entity);
        return modelMapper.map(savedEntity, CircleSettings.class);
    }

    @Override
    public Optional<CircleSettings> findByCircleId(UUID circleId) {
        log.debug("Finding circle settings by circleId: {}", circleId);
        return springDataCircleSettingsRepository.findByCircleId(circleId)
                .map(entity -> modelMapper.map(entity, CircleSettings.class));
    }
}
