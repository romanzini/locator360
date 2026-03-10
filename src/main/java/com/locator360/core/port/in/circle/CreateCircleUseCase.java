package com.locator360.core.port.in.circle;

import com.locator360.core.port.in.dto.input.CreateCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleOutputDto;

import java.util.UUID;

public interface CreateCircleUseCase {

    CircleOutputDto execute(UUID userId, CreateCircleInputDto input);
}
