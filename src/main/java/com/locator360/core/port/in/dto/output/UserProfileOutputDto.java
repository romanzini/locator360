package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class UserProfileOutputDto {

  UUID id;
  String email;
  String phoneNumber;
  String fullName;
  String firstName;
  String lastName;
  LocalDate birthDate;
  String gender;
  String profilePhotoUrl;
  String preferredLanguage;
  String timezone;
  String distanceUnit;
  String status;
  Instant createdAt;
  Instant updatedAt;
}
