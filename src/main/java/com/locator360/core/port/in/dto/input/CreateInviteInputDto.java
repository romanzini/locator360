package com.locator360.core.port.in.dto.input;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInviteInputDto {

    @Email
    private String targetEmail;

    private String targetPhone;

    private Instant expiresAt;
}
