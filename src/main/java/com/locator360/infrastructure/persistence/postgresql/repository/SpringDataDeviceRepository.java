package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.infrastructure.persistence.postgresql.entity.DeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataDeviceRepository extends JpaRepository<DeviceJpaEntity, UUID> {

  Optional<DeviceJpaEntity> findByUserIdAndPlatform(UUID userId, String platform);

  List<DeviceJpaEntity> findByUserId(UUID userId);
}
