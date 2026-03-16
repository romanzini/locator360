package com.locator360.core.port.in.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PauseLocationInputDto {

  private Instant pausedUntil;
}
