package com.locator360.core.port.in.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginWithPhoneInputDto {

  @NotBlank
  private String phoneNumber;

  @NotBlank
  private String verificationCode;
}
