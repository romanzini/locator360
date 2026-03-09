package com.locator360.core.port.in.dto.output;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class LoginOutputDto {

  String accessToken;
  String refreshToken;
  String tokenType;
  int expiresIn;
  UserInfo user;

  @Value
  @Builder
  public static class UserInfo {

    UUID id;
    String email;
    String phoneNumber;
    String fullName;
    String preferredLanguage;
    String timezone;
    String distanceUnit;
    String status;
    Instant createdAt;
    Instant updatedAt;
  }
}
