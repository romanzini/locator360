package com.locator360.core.port.in.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPasswordResetInputDto {

  @NotBlank
  private String token;

  @NotBlank
  @Size(min = 6)
  private String newPassword;
}