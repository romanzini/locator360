package com.locator360.core.port.in.user;

import com.locator360.core.port.in.dto.output.DeviceOutputDto;

import java.util.List;
import java.util.UUID;

public interface ListUserDevicesUseCase {

    List<DeviceOutputDto> execute(UUID userId);
}
