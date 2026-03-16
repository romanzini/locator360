package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class MemberLocationOutputDto {

    UUID userId;
    String fullName;
    String profilePhotoUrl;
    Double latitude;
    Double longitude;
    Double accuracy;
    Double speed;
    Boolean isMoving;
    Integer batteryLevel;
    Instant lastUpdatedAt;
    SharingStatus sharingStatus;
}
