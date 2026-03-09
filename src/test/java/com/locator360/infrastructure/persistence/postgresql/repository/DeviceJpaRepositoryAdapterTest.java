package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.user.Device;
import com.locator360.core.domain.user.Platform;
import com.locator360.infrastructure.persistence.postgresql.entity.DeviceJpaEntity;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceJpaRepositoryAdapterTest {

  @Mock
  private SpringDataDeviceRepository springDataDeviceRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private DeviceJpaRepositoryAdapter deviceJpaRepositoryAdapter;

  private Device createDomainDevice() {
    return Device.restore(
        UUID.randomUUID(),
        UUID.randomUUID(),
        Platform.ANDROID,
        "Pixel 7",
        "14",
        "1.0.0",
        "push_token_123",
        true,
        Instant.now(),
        Instant.now(),
        Instant.now());
  }

  private DeviceJpaEntity createJpaEntity() {
    DeviceJpaEntity entity = new DeviceJpaEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserId(UUID.randomUUID());
    entity.setPlatform("ANDROID");
    entity.setDeviceModel("Pixel 7");
    entity.setOsVersion("14");
    entity.setAppVersion("1.0.0");
    entity.setPushToken("push_token_123");
    entity.setActive(true);
    entity.setLastSeenAt(Instant.now());
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());
    return entity;
  }

  @Nested
  @DisplayName("save")
  class SaveTests {

    @Test
    @DisplayName("should map domain to entity, save, and map back to domain")
    void shouldSaveAndReturnDomain() {
      Device domainDevice = createDomainDevice();
      DeviceJpaEntity jpaEntity = createJpaEntity();

      when(modelMapper.map(domainDevice, DeviceJpaEntity.class)).thenReturn(jpaEntity);
      when(springDataDeviceRepository.save(jpaEntity)).thenReturn(jpaEntity);
      when(modelMapper.map(jpaEntity, Device.class)).thenReturn(domainDevice);

      Device result = deviceJpaRepositoryAdapter.save(domainDevice);

      assertNotNull(result);
      verify(springDataDeviceRepository).save(jpaEntity);
      verify(modelMapper).map(domainDevice, DeviceJpaEntity.class);
      verify(modelMapper).map(jpaEntity, Device.class);
    }
  }

  @Nested
  @DisplayName("findByUserIdAndPlatform")
  class FindByUserIdAndPlatformTests {

    @Test
    @DisplayName("should return device when found")
    void shouldReturnDevice() {
      UUID userId = UUID.randomUUID();
      DeviceJpaEntity jpaEntity = createJpaEntity();
      Device domainDevice = createDomainDevice();

      when(springDataDeviceRepository.findByUserIdAndPlatform(userId, "ANDROID"))
          .thenReturn(Optional.of(jpaEntity));
      when(modelMapper.map(jpaEntity, Device.class)).thenReturn(domainDevice);

      Optional<Device> result = deviceJpaRepositoryAdapter
          .findByUserIdAndPlatform(userId, Platform.ANDROID);

      assertTrue(result.isPresent());
      verify(springDataDeviceRepository).findByUserIdAndPlatform(userId, "ANDROID");
    }

    @Test
    @DisplayName("should return empty when not found")
    void shouldReturnEmpty() {
      UUID userId = UUID.randomUUID();
      when(springDataDeviceRepository.findByUserIdAndPlatform(userId, "IOS"))
          .thenReturn(Optional.empty());

      Optional<Device> result = deviceJpaRepositoryAdapter
          .findByUserIdAndPlatform(userId, Platform.IOS);

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("findByUserId")
  class FindByUserIdTests {

    @Test
    @DisplayName("should return list of devices")
    void shouldReturnDevices() {
      UUID userId = UUID.randomUUID();
      DeviceJpaEntity jpaEntity = createJpaEntity();
      Device domainDevice = createDomainDevice();

      when(springDataDeviceRepository.findByUserId(userId))
          .thenReturn(List.of(jpaEntity));
      when(modelMapper.map(jpaEntity, Device.class)).thenReturn(domainDevice);

      List<Device> result = deviceJpaRepositoryAdapter.findByUserId(userId);

      assertEquals(1, result.size());
      verify(springDataDeviceRepository).findByUserId(userId);
    }
  }
}
