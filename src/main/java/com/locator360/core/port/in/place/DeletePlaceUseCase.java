package com.locator360.core.port.in.place;

import java.util.UUID;

public interface DeletePlaceUseCase {

  void execute(UUID userId, UUID circleId, UUID placeId);
}
