package com.locator360.core.port.in.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileInputDto {

  private String fullName;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private String gender;
  private String profilePhotoUrl;
  private String preferredLanguage;
  private String timezone;
  private String distanceUnit;
}
