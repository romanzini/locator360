package com.locator360.core.port.in.dto.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamLocationInputDto {

    private UUID circleId;

    @NotEmpty
    @Valid
    private List<LocationPointDto> events;
}
