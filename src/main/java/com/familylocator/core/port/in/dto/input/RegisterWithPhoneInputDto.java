package com.familylocator.core.port.in.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWithPhoneInputDto {

  @NotBlank
  private String phoneNumber;

  @NotBlank
  private String verificationCode;

  @NotBlank
  private String fullName;
}
