package com.locator360.core.port.out;

import com.locator360.core.domain.place.PlaceAlertPolicy;

import java.util.Optional;
import java.util.UUID;

public interface PlaceAlertPolicyRepository {

  PlaceAlertPolicy save(PlaceAlertPolicy policy);

  Optional<PlaceAlertPolicy> findByPlaceId(UUID placeId);

  void deleteByPlaceId(UUID placeId);
}
