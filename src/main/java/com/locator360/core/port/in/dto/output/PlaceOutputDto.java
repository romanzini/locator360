package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class PlaceOutputDto {

  UUID id;
  UUID circleId;
  String name;
  String type;
  String addressText;
  double latitude;
  double longitude;
  double radiusMeters;
  boolean active;
  UUID createdByUserId;
  Instant createdAt;
  Instant updatedAt;
}
