package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceType;
import com.locator360.core.port.out.PlaceRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.PlaceJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PlaceJpaRepositoryAdapter implements PlaceRepository {

  private final SpringDataPlaceRepository springDataPlaceRepository;

  @Override
  public Place save(Place place) {
    log.debug("Saving place: {}", place.getId());
    PlaceJpaEntity entity = toJpaEntity(place);
    PlaceJpaEntity savedEntity = springDataPlaceRepository.save(entity);
    log.debug("Place saved successfully: {}", savedEntity.getId());
    return toDomain(savedEntity);
  }

  @Override
  public Optional<Place> findById(UUID id) {
    log.debug("Finding place by id: {}", id);
    return springDataPlaceRepository.findById(id)
        .map(this::toDomain);
  }

  @Override
  public List<Place> findByCircleId(UUID circleId) {
    log.debug("Finding places by circle id: {}", circleId);
    return springDataPlaceRepository.findByCircleId(circleId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Place> findActiveByCircleId(UUID circleId) {
    log.debug("Finding active places by circle id: {}", circleId);
    return springDataPlaceRepository.findByCircleIdAndActiveTrue(circleId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(UUID id) {
    log.debug("Deleting place by id: {}", id);
    springDataPlaceRepository.deleteById(id);
  }

  private Place toDomain(PlaceJpaEntity entity) {
    return Place.restore(
        entity.getId(),
        entity.getCircleId(),
        entity.getName(),
        PlaceType.valueOf(entity.getType()),
        entity.getAddressText(),
        entity.getLatitude(),
        entity.getLongitude(),
        entity.getRadiusMeters(),
        entity.isActive(),
        entity.getCreatedByUserId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private PlaceJpaEntity toJpaEntity(Place place) {
    PlaceJpaEntity entity = new PlaceJpaEntity();
    entity.setId(place.getId());
    entity.setCircleId(place.getCircleId());
    entity.setName(place.getName());
    entity.setType(place.getType().name());
    entity.setAddressText(place.getAddressText());
    entity.setLatitude(place.getLatitude());
    entity.setLongitude(place.getLongitude());
    entity.setRadiusMeters(place.getRadiusMeters());
    entity.setActive(place.isActive());
    entity.setCreatedByUserId(place.getCreatedByUserId());
    entity.setCreatedAt(place.getCreatedAt());
    entity.setUpdatedAt(place.getUpdatedAt());
    return entity;
  }
}
