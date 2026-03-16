package com.locator360.core.port.in.place;

import com.locator360.core.port.in.dto.input.CreatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;

import java.util.UUID;

public interface CreatePlaceUseCase {

  PlaceOutputDto execute(UUID userId, UUID circleId, CreatePlaceInputDto input);
}
