package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.user.Device;
import com.locator360.core.domain.user.Platform;
import com.locator360.core.port.out.DeviceRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.DeviceJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DeviceJpaRepositoryAdapter implements DeviceRepository {

  private final SpringDataDeviceRepository springDataDeviceRepository;
  private final ModelMapper modelMapper;

  @Override
  public Device save(Device device) {
    log.debug("Saving device: {}", device.getId());
    DeviceJpaEntity entity = modelMapper.map(device, DeviceJpaEntity.class);
    DeviceJpaEntity savedEntity = springDataDeviceRepository.save(entity);
    return modelMapper.map(savedEntity, Device.class);
  }

  @Override
  public Optional<Device> findByUserIdAndPlatform(UUID userId, Platform platform) {
    log.debug("Finding device by userId: {} and platform: {}", userId, platform);
    return springDataDeviceRepository
        .findByUserIdAndPlatform(userId, platform.name())
        .map(entity -> modelMapper.map(entity, Device.class));
  }

  @Override
  public List<Device> findByUserId(UUID userId) {
    log.debug("Finding devices by userId: {}", userId);
    return springDataDeviceRepository.findByUserId(userId).stream()
        .map(entity -> modelMapper.map(entity, Device.class))
        .collect(Collectors.toList());
  }
}
