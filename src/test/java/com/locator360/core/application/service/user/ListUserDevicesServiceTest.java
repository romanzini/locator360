package com.locator360.core.application.service.user;

import com.locator360.core.domain.user.Device;
import com.locator360.core.domain.user.Platform;
import com.locator360.core.port.in.dto.output.DeviceOutputDto;
import com.locator360.core.port.out.DeviceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserDevicesServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private ListUserDevicesService listUserDevicesService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("should return list of devices for user")
        void shouldReturnListOfDevicesForUser() {
            UUID userId = UUID.randomUUID();

            Device device1 = Device.restore(
                    UUID.randomUUID(), userId, Platform.ANDROID, "Pixel 7",
                    "14", "1.0.0", "token1", true,
                    Instant.now(), Instant.now(), Instant.now());

            Device device2 = Device.restore(
                    UUID.randomUUID(), userId, Platform.IOS, "iPhone 15",
                    "17.2", "1.0.0", "token2", true,
                    Instant.now(), Instant.now(), Instant.now());

            DeviceOutputDto dto1 = DeviceOutputDto.builder()
                    .id(device1.getId())
                    .platform("ANDROID")
                    .deviceModel("Pixel 7")
                    .active(true)
                    .build();

            DeviceOutputDto dto2 = DeviceOutputDto.builder()
                    .id(device2.getId())
                    .platform("IOS")
                    .deviceModel("iPhone 15")
                    .active(true)
                    .build();

            when(deviceRepository.findByUserId(userId)).thenReturn(List.of(device1, device2));
            when(modelMapper.map(device1, DeviceOutputDto.class)).thenReturn(dto1);
            when(modelMapper.map(device2, DeviceOutputDto.class)).thenReturn(dto2);

            List<DeviceOutputDto> result = listUserDevicesService.execute(userId);

            assertEquals(2, result.size());
            assertEquals("ANDROID", result.get(0).getPlatform());
            assertEquals("IOS", result.get(1).getPlatform());
        }

        @Test
        @DisplayName("should return empty list when user has no devices")
        void shouldReturnEmptyListWhenNoDevices() {
            UUID userId = UUID.randomUUID();

            when(deviceRepository.findByUserId(userId)).thenReturn(List.of());

            List<DeviceOutputDto> result = listUserDevicesService.execute(userId);

            assertTrue(result.isEmpty());
        }
    }
}
