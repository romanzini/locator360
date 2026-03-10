package com.locator360.core.port.in.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCircleInputDto {

    @NotBlank
    private String name;

    private String description;

    private String photoUrl;

    private String colorHex;

    private String privacyLevel;
}
