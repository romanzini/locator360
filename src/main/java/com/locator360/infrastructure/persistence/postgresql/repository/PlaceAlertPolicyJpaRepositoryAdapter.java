package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.place.PlaceAlertPolicy;
import com.locator360.core.domain.place.TargetType;
import com.locator360.core.port.out.PlaceAlertPolicyRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.PlaceAlertPolicyJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PlaceAlertPolicyJpaRepositoryAdapter implements PlaceAlertPolicyRepository {

  private final SpringDataPlaceAlertPolicyRepository springDataPlaceAlertPolicyRepository;

  @Override
  public PlaceAlertPolicy save(PlaceAlertPolicy policy) {
    log.debug("Saving place alert policy: {}", policy.getId());
    PlaceAlertPolicyJpaEntity entity = toJpaEntity(policy);
    PlaceAlertPolicyJpaEntity savedEntity = springDataPlaceAlertPolicyRepository.save(entity);
    log.debug("Place alert policy saved successfully: {}", savedEntity.getId());
    return toDomain(savedEntity);
  }

  @Override
  public Optional<PlaceAlertPolicy> findByPlaceId(UUID placeId) {
    log.debug("Finding place alert policy by place id: {}", placeId);
    return springDataPlaceAlertPolicyRepository.findByPlaceId(placeId)
        .map(this::toDomain);
  }

  @Override
  public void deleteByPlaceId(UUID placeId) {
    log.debug("Deleting place alert policies by place id: {}", placeId);
    springDataPlaceAlertPolicyRepository.deleteByPlaceId(placeId);
  }

  private PlaceAlertPolicy toDomain(PlaceAlertPolicyJpaEntity entity) {
    return PlaceAlertPolicy.restore(
        entity.getId(),
        entity.getPlaceId(),
        entity.getCircleId(),
        entity.isAlertOnEnter(),
        entity.isAlertOnExit(),
        entity.getDaysOfWeek(),
        entity.getStartTime(),
        entity.getEndTime(),
        TargetType.valueOf(entity.getTargetType()),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private PlaceAlertPolicyJpaEntity toJpaEntity(PlaceAlertPolicy policy) {
    PlaceAlertPolicyJpaEntity entity = new PlaceAlertPolicyJpaEntity();
    entity.setId(policy.getId());
    entity.setPlaceId(policy.getPlaceId());
    entity.setCircleId(policy.getCircleId());
    entity.setAlertOnEnter(policy.isAlertOnEnter());
    entity.setAlertOnExit(policy.isAlertOnExit());
    entity.setDaysOfWeek(policy.getDaysOfWeek());
    entity.setStartTime(policy.getStartTime());
    entity.setEndTime(policy.getEndTime());
    entity.setTargetType(policy.getTargetType().name());
    entity.setCreatedAt(policy.getCreatedAt());
    entity.setUpdatedAt(policy.getUpdatedAt());
    return entity;
  }
}
