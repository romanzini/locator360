package com.locator360.core.application.service.auth;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

  @Mock
  private DeviceRepository deviceRepository;

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private Counter counter;

  @InjectMocks
  private LogoutService logoutService;

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    private UUID userId;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Test
    @DisplayName("should deactivate all user devices on logout")
    void shouldDeactivateAllDevices() {
      Device device1 = Device.restore(
          UUID.randomUUID(), userId, Platform.ANDROID, "Pixel 7", "14",
          "1.0.0", "push_token_1", true, Instant.now(), Instant.now(), Instant.now());
      Device device2 = Device.restore(
          UUID.randomUUID(), userId, Platform.IOS, "iPhone 15", "17",
          "1.0.0", "push_token_2", true, Instant.now(), Instant.now(), Instant.now());

      when(deviceRepository.findByUserId(userId)).thenReturn(List.of(device1, device2));

      logoutService.execute(userId);

      assertFalse(device1.isActive());
      assertFalse(device2.isActive());
      verify(deviceRepository, times(2)).save(any(Device.class));
    }

    @Test
    @DisplayName("should handle logout when no devices found")
    void shouldHandleNoDevices() {
      when(deviceRepository.findByUserId(userId)).thenReturn(List.of());

      assertDoesNotThrow(() -> logoutService.execute(userId));
      verify(deviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should increment logout counter")
    void shouldIncrementCounter() {
      when(deviceRepository.findByUserId(userId)).thenReturn(List.of());

      logoutService.execute(userId);

      verify(meterRegistry).counter("users.logout");
      verify(counter).increment();
    }
  }
}
