package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.place.PlaceAlertTarget;
import com.locator360.core.port.out.PlaceAlertTargetRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.PlaceAlertTargetJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PlaceAlertTargetJpaRepositoryAdapter implements PlaceAlertTargetRepository {

  private final SpringDataPlaceAlertTargetRepository springDataPlaceAlertTargetRepository;

  @Override
  public PlaceAlertTarget save(PlaceAlertTarget target) {
    log.debug("Saving place alert target: {}", target.getId());
    PlaceAlertTargetJpaEntity entity = toJpaEntity(target);
    PlaceAlertTargetJpaEntity savedEntity = springDataPlaceAlertTargetRepository.save(entity);
    log.debug("Place alert target saved successfully: {}", savedEntity.getId());
    return toDomain(savedEntity);
  }

  @Override
  public List<PlaceAlertTarget> findByPolicyId(UUID policyId) {
    log.debug("Finding place alert targets by policy id: {}", policyId);
    return springDataPlaceAlertTargetRepository.findByPolicyId(policyId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteByPolicyId(UUID policyId) {
    log.debug("Deleting place alert targets by policy id: {}", policyId);
    springDataPlaceAlertTargetRepository.deleteByPolicyId(policyId);
  }

  private PlaceAlertTarget toDomain(PlaceAlertTargetJpaEntity entity) {
    return PlaceAlertTarget.restore(
        entity.getId(),
        entity.getPolicyId(),
        entity.getUserId());
  }

  private PlaceAlertTargetJpaEntity toJpaEntity(PlaceAlertTarget target) {
    PlaceAlertTargetJpaEntity entity = new PlaceAlertTargetJpaEntity();
    entity.setId(target.getId());
    entity.setPolicyId(target.getPolicyId());
    entity.setUserId(target.getUserId());
    return entity;
  }
}
