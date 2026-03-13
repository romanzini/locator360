package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class DeviceOutputDto {

    UUID id;
    String platform;
    String deviceModel;
    String osVersion;
    String appVersion;
    boolean active;
    Instant lastSeenAt;
    Instant createdAt;
}
