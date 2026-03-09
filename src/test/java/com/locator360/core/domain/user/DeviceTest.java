package com.locator360.core.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeviceTest {

  // ─── Factory create() ───────────────────────────────────────────

  @Nested
  @DisplayName("Device.create()")
  class CreateTests {

    @Test
    @DisplayName("should create device with all fields")
    void shouldCreateDeviceWithAllFields() {
      UUID userId = UUID.randomUUID();

      Device device = Device.create(userId, Platform.ANDROID, "Pixel 7",
          "14.0", "1.0.0", "fcm_token_123");

      assertNotNull(device.getId());
      assertEquals(userId, device.getUserId());
      assertEquals(Platform.ANDROID, device.getPlatform());
      assertEquals("Pixel 7", device.getDeviceModel());
      assertEquals("14.0", device.getOsVersion());
      assertEquals("1.0.0", device.getAppVersion());
      assertEquals("fcm_token_123", device.getPushToken());
      assertTrue(device.isActive());
      assertNotNull(device.getLastSeenAt());
      assertNotNull(device.getCreatedAt());
      assertNotNull(device.getUpdatedAt());
    }

    @Test
    @DisplayName("should create device with nullable fields as null")
    void shouldCreateDeviceWithNullableFields() {
      UUID userId = UUID.randomUUID();

      Device device = Device.create(userId, Platform.IOS, null, null, null, null);

      assertNotNull(device.getId());
      assertEquals(userId, device.getUserId());
      assertEquals(Platform.IOS, device.getPlatform());
      assertNull(device.getDeviceModel());
      assertNull(device.getOsVersion());
      assertNull(device.getAppVersion());
      assertNull(device.getPushToken());
      assertTrue(device.isActive());
    }

    @Test
    @DisplayName("should throw when user ID is null")
    void shouldThrowWhenUserIdIsNull() {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> Device.create(null, Platform.ANDROID, "Pixel 7", "14.0", "1.0.0", null));

      assertTrue(ex.getMessage().toLowerCase().contains("user id"));
    }

    @Test
    @DisplayName("should throw when platform is null")
    void shouldThrowWhenPlatformIsNull() {
      UUID userId = UUID.randomUUID();

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> Device.create(userId, null, "Pixel 7", "14.0", "1.0.0", null));

      assertTrue(ex.getMessage().toLowerCase().contains("platform"));
    }

    @Test
    @DisplayName("should generate unique IDs for different devices")
    void shouldGenerateUniqueIds() {
      UUID userId = UUID.randomUUID();

      Device device1 = Device.create(userId, Platform.ANDROID, null, null, null, null);
      Device device2 = Device.create(userId, Platform.IOS, null, null, null, null);

      assertNotEquals(device1.getId(), device2.getId());
    }
  }

  // ─── Factory restore() ──────────────────────────────────────────

  @Nested
  @DisplayName("Device.restore()")
  class RestoreTests {

    @Test
    @DisplayName("should restore device with all fields")
    void shouldRestoreDeviceWithAllFields() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Instant now = Instant.now();

      Device device = Device.restore(id, userId, Platform.WEB, "Chrome",
          "120.0", "2.0.0", "web_push_token", true, now, now, now);

      assertEquals(id, device.getId());
      assertEquals(userId, device.getUserId());
      assertEquals(Platform.WEB, device.getPlatform());
      assertEquals("Chrome", device.getDeviceModel());
      assertEquals("120.0", device.getOsVersion());
      assertEquals("2.0.0", device.getAppVersion());
      assertEquals("web_push_token", device.getPushToken());
      assertTrue(device.isActive());
      assertEquals(now, device.getLastSeenAt());
      assertEquals(now, device.getCreatedAt());
      assertEquals(now, device.getUpdatedAt());
    }
  }

  // ─── Business methods ───────────────────────────────────────────

  @Nested
  @DisplayName("Business methods")
  class BusinessMethodTests {

    @Test
    @DisplayName("should deactivate device")
    void shouldDeactivateDevice() {
      Device device = Device.create(UUID.randomUUID(), Platform.ANDROID,
          null, null, null, null);

      device.deactivate();

      assertFalse(device.isActive());
    }

    @Test
    @DisplayName("should activate device")
    void shouldActivateDevice() {
      Device device = Device.create(UUID.randomUUID(), Platform.ANDROID,
          null, null, null, null);
      device.deactivate();

      device.activate();

      assertTrue(device.isActive());
    }

    @Test
    @DisplayName("should update last seen")
    void shouldUpdateLastSeen() {
      Device device = Device.create(UUID.randomUUID(), Platform.ANDROID,
          null, null, null, null);
      Instant before = device.getLastSeenAt();

      device.updateLastSeen();

      assertNotNull(device.getLastSeenAt());
    }

    @Test
    @DisplayName("should update push token")
    void shouldUpdatePushToken() {
      Device device = Device.create(UUID.randomUUID(), Platform.ANDROID,
          null, null, null, null);

      device.updatePushToken("new_token_xyz");

      assertEquals("new_token_xyz", device.getPushToken());
    }

    @Test
    @DisplayName("should update app version")
    void shouldUpdateAppVersion() {
      Device device = Device.create(UUID.randomUUID(), Platform.ANDROID,
          null, null, "1.0.0", null);

      device.updateAppVersion("2.0.0");

      assertEquals("2.0.0", device.getAppVersion());
    }
  }
}
