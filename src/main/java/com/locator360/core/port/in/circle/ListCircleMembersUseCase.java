package com.locator360.core.port.in.circle;

import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;

import java.util.List;
import java.util.UUID;

public interface ListCircleMembersUseCase {

    List<CircleMemberOutputDto> execute(UUID userId, UUID circleId);
}
