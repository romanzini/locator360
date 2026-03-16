package com.locator360.core.port.out;

import com.locator360.core.domain.place.Place;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository {

  Place save(Place place);

  Optional<Place> findById(UUID id);

  List<Place> findByCircleId(UUID circleId);

  List<Place> findActiveByCircleId(UUID circleId);

  void deleteById(UUID id);
}
