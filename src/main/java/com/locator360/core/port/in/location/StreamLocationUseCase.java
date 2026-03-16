package com.locator360.core.port.in.location;

import com.locator360.core.port.in.dto.input.StreamLocationInputDto;

import java.util.UUID;

public interface StreamLocationUseCase {

    void execute(UUID userId, StreamLocationInputDto input);
}
