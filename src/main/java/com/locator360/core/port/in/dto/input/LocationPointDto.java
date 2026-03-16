package com.locator360.core.port.in.dto.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationPointDto {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Double accuracyMeters;

    private Double speedMps;

    private Double headingDegrees;

    private Double altitudeMeters;

    @NotNull
    private String source;

    @NotNull
    private Instant recordedAt;

    private Boolean isMoving;

    private Integer batteryLevel;
}
