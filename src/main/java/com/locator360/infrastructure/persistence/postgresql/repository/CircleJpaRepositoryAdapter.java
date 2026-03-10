package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.circle.Circle;
import com.locator360.core.domain.circle.PrivacyLevel;
import com.locator360.core.port.out.CircleRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.CircleJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CircleJpaRepositoryAdapter implements CircleRepository {

    private final SpringDataCircleRepository springDataCircleRepository;
    private final ModelMapper modelMapper;

    @Override
    public Circle save(Circle circle) {
        log.debug("Saving circle: {}", circle.getId());
        CircleJpaEntity entity = toJpaEntity(circle);
        CircleJpaEntity savedEntity = springDataCircleRepository.save(entity);
        log.debug("Circle saved successfully: {}", savedEntity.getId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Circle> findById(UUID id) {
        log.debug("Finding circle by id: {}", id);
        return springDataCircleRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Circle> findByCreatedByUserId(UUID userId) {
        log.debug("Finding circles by creator user id: {}", userId);
        return springDataCircleRepository.findByCreatedByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Circle toDomain(CircleJpaEntity entity) {
        return Circle.restore(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPhotoUrl(),
                entity.getColorHex(),
                PrivacyLevel.valueOf(entity.getPrivacyLevel()),
                entity.getCreatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private CircleJpaEntity toJpaEntity(Circle circle) {
        CircleJpaEntity entity = new CircleJpaEntity();
        entity.setId(circle.getId());
        entity.setName(circle.getName());
        entity.setDescription(circle.getDescription());
        entity.setPhotoUrl(circle.getPhotoUrl());
        entity.setColorHex(circle.getColorHex());
        entity.setPrivacyLevel(circle.getPrivacyLevel().name());
        entity.setCreatedByUserId(circle.getCreatedByUserId());
        entity.setCreatedAt(circle.getCreatedAt());
        entity.setUpdatedAt(circle.getUpdatedAt());
        return entity;
    }
}
