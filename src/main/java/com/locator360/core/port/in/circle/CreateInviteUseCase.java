package com.locator360.core.port.in.circle;

import com.locator360.core.port.in.dto.input.CreateInviteInputDto;
import com.locator360.core.port.in.dto.output.InviteOutputDto;

import java.util.UUID;

public interface CreateInviteUseCase {

    InviteOutputDto execute(UUID userId, UUID circleId, CreateInviteInputDto input);
}
