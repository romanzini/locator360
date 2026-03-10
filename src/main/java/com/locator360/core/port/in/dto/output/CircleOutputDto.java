package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class CircleOutputDto {

    UUID id;
    String name;
    String description;
    String photoUrl;
    String colorHex;
    String privacyLevel;
    UUID createdByUserId;
    String role;
    Instant createdAt;
    Instant updatedAt;
}
