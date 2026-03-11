package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class InviteOutputDto {

    UUID id;
    UUID circleId;
    UUID invitedByUserId;
    String targetEmail;
    String targetPhone;
    String inviteCode;
    String status;
    UUID acceptedByUserId;
    Instant expiresAt;
    Instant createdAt;
    Instant updatedAt;
}
