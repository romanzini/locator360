package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class CircleMemberOutputDto {

    UUID id;
    UUID circleId;
    UUID userId;
    String role;
    String status;
    Instant joinedAt;
}
