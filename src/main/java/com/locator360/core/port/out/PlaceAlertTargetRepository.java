package com.locator360.core.port.out;

import com.locator360.core.domain.place.PlaceAlertTarget;

import java.util.List;
import java.util.UUID;

public interface PlaceAlertTargetRepository {

  PlaceAlertTarget save(PlaceAlertTarget target);

  List<PlaceAlertTarget> findByPolicyId(UUID policyId);

  void deleteByPolicyId(UUID policyId);
}
