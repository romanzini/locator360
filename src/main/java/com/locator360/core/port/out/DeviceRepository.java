package com.locator360.core.port.out;

import com.locator360.core.domain.user.Device;
import com.locator360.core.domain.user.Platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository {

  Device save(Device device);

  Optional<Device> findById(UUID id);

  Optional<Device> findByUserIdAndPlatform(UUID userId, Platform platform);

  List<Device> findByUserId(UUID userId);
}
