package com.locator360.core.port.in.place;

import com.locator360.core.port.in.dto.output.PlaceOutputDto;

import java.util.List;
import java.util.UUID;

public interface ListPlacesUseCase {

  List<PlaceOutputDto> execute(UUID userId, UUID circleId);
}
