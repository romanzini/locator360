package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.Device;
import com.locator360.core.port.in.auth.LogoutUseCase;
import com.locator360.core.port.out.DeviceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LogoutService implements LogoutUseCase {

  private final DeviceRepository deviceRepository;
  private final MeterRegistry meterRegistry;

  @Override
  public void execute(UUID userId) {
    log.debug("Logout for user: {}", userId);

    List<Device> devices = deviceRepository.findByUserId(userId);
    for (Device device : devices) {
      device.deactivate();
      deviceRepository.save(device);
    }

    log.info("User logged out: {}", userId);
    meterRegistry.counter("users.logout").increment();
  }
}
