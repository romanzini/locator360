package com.locator360.core.port.in.circle;

import com.locator360.core.port.in.dto.input.JoinCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;

import java.util.UUID;

public interface JoinCircleUseCase {

    CircleMemberOutputDto execute(UUID userId, JoinCircleInputDto input);
}
