package com.locator360.core.port.in.dto.input;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordResetInputDto {

  @Email
  private String email;

  private String phoneNumber;

  @AssertTrue(message = "Either email or phoneNumber must be provided")
  public boolean hasEmailOrPhoneNumber() {
    return hasText(email) || hasText(phoneNumber);
  }

  public boolean hasEmail() {
    return hasText(email);
  }

  public boolean hasPhoneNumber() {
    return hasText(phoneNumber);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}