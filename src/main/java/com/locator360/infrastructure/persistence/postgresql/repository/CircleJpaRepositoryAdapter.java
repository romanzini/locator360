package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.circle.Circle;
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
        CircleJpaEntity entity = modelMapper.map(circle, CircleJpaEntity.class);
        CircleJpaEntity savedEntity = springDataCircleRepository.save(entity);
        return modelMapper.map(savedEntity, Circle.class);
    }

    @Override
    public Optional<Circle> findById(UUID id) {
        log.debug("Finding circle by id: {}", id);
        return springDataCircleRepository.findById(id)
                .map(entity -> modelMapper.map(entity, Circle.class));
    }

    @Override
    public List<Circle> findByCreatedByUserId(UUID userId) {
        log.debug("Finding circles by creator user id: {}", userId);
        return springDataCircleRepository.findByCreatedByUserId(userId).stream()
                .map(entity -> modelMapper.map(entity, Circle.class))
                .collect(Collectors.toList());
    }
}
