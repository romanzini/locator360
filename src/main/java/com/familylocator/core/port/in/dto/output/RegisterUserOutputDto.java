package com.familylocator.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class RegisterUserOutputDto {

  UUID id;
  String email;
  String phoneNumber;
  String fullName;
  String firstName;
  String lastName;
  String preferredLanguage;
  String timezone;
  String distanceUnit;
  String status;
  Instant createdAt;
  Instant updatedAt;
}
