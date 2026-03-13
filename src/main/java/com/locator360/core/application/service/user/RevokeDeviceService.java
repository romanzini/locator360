package com.locator360.core.application.service.user;

import com.locator360.core.domain.user.Device;
import com.locator360.core.port.in.user.RevokeDeviceUseCase;
import com.locator360.core.port.out.DeviceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RevokeDeviceService implements RevokeDeviceUseCase {

    private final DeviceRepository deviceRepository;
    private final MeterRegistry meterRegistry;

    @Override
    public void execute(UUID userId, UUID deviceId) {
        log.debug("Revoking device: {} for user: {}", deviceId, userId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        if (!device.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Device does not belong to this user");
        }

        if (!device.isActive()) {
            throw new IllegalStateException("Device is already inactive");
        }

        device.deactivate();
        deviceRepository.save(device);

        meterRegistry.counter("devices.revoked").increment();
        log.info("Device revoked: {} for user: {}", deviceId, userId);
    }
}
