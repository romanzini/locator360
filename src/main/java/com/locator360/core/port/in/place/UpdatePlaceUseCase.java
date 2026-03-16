package com.locator360.core.port.in.place;

import com.locator360.core.port.in.dto.input.UpdatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;

import java.util.UUID;

public interface UpdatePlaceUseCase {

  PlaceOutputDto execute(UUID userId, UUID circleId, UUID placeId, UpdatePlaceInputDto input);
}
