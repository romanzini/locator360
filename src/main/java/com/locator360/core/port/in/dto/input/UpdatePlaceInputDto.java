package com.locator360.core.port.in.dto.input;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlaceInputDto {

  private String name;

  private String type;

  private String addressText;

  @Range(min = -90, max = 90)
  private Double latitude;

  @Range(min = -180, max = 180)
  private Double longitude;

  @Positive
  private Double radiusMeters;
}
