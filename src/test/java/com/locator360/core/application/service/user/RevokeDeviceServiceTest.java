package com.locator360.core.application.service.user;

import com.locator360.core.domain.user.Device;
import com.locator360.core.domain.user.Platform;
import com.locator360.core.port.out.DeviceRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokeDeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private RevokeDeviceService revokeDeviceService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID userId;
        private UUID deviceId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            deviceId = UUID.randomUUID();

            lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should deactivate device successfully")
        void shouldDeactivateDeviceSuccessfully() {
            Device device = Device.restore(
                    deviceId, userId, Platform.ANDROID, "Pixel 7",
                    "14", "1.0.0", "token1", true,
                    Instant.now(), Instant.now(), Instant.now());

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceRepository.save(any())).thenReturn(device);

            revokeDeviceService.execute(userId, deviceId);

            assertFalse(device.isActive());
            verify(deviceRepository).save(device);
        }

        @Test
        @DisplayName("should throw when device not found")
        void shouldThrowWhenDeviceNotFound() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> revokeDeviceService.execute(userId, deviceId));

            verify(deviceRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when device belongs to another user")
        void shouldThrowWhenDeviceBelongsToAnotherUser() {
            UUID otherUserId = UUID.randomUUID();
            Device device = Device.restore(
                    deviceId, otherUserId, Platform.ANDROID, "Pixel 7",
                    "14", "1.0.0", "token1", true,
                    Instant.now(), Instant.now(), Instant.now());

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThrows(IllegalArgumentException.class,
                    () -> revokeDeviceService.execute(userId, deviceId));

            verify(deviceRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when device is already inactive")
        void shouldThrowWhenDeviceAlreadyInactive() {
            Device device = Device.restore(
                    deviceId, userId, Platform.ANDROID, "Pixel 7",
                    "14", "1.0.0", "token1", false,
                    Instant.now(), Instant.now(), Instant.now());

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThrows(IllegalStateException.class,
                    () -> revokeDeviceService.execute(userId, deviceId));

            verify(deviceRepository, never()).save(any());
        }
    }
}
