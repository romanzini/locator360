package com.locator360.core.port.in.location;

import com.locator360.core.port.in.dto.output.MemberLocationOutputDto;

import java.util.List;
import java.util.UUID;

public interface GetCircleMembersLocationUseCase {

    List<MemberLocationOutputDto> execute(UUID userId, UUID circleId);
}
