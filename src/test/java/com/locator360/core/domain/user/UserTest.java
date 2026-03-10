package com.locator360.core.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

  // ─── Factory create() ───────────────────────────────────────────

  @Nested
  @DisplayName("User.create()")
  class CreateTests {

    @Test
    @DisplayName("should create user with email only")
    void shouldCreateUserWithEmailOnly() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      assertNotNull(user.getId());
      assertEquals("maria@example.com", user.getEmail());
      assertNull(user.getPhoneNumber());
      assertEquals("Maria Oliveira", user.getFullName());
      assertEquals("Maria", user.getFirstName());
      assertEquals("Oliveira", user.getLastName());
      assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
      assertNotNull(user.getCreatedAt());
      assertNotNull(user.getUpdatedAt());
    }

    @Test
    @DisplayName("should create user with phone only")
    void shouldCreateUserWithPhoneOnly() {
      User user = User.create(null, "+5511999999999", "João da Silva");

      assertNotNull(user.getId());
      assertNull(user.getEmail());
      assertEquals("+5511999999999", user.getPhoneNumber());
      assertEquals("João da Silva", user.getFullName());
      assertEquals("João", user.getFirstName());
      assertEquals("da Silva", user.getLastName());
      assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
    }

    @Test
    @DisplayName("should create user with email and phone")
    void shouldCreateUserWithEmailAndPhone() {
      User user = User.create("maria@example.com", "+5511999999999", "Maria Oliveira");

      assertEquals("maria@example.com", user.getEmail());
      assertEquals("+5511999999999", user.getPhoneNumber());
    }

    @Test
    @DisplayName("should throw when email and phone are both null")
    void shouldThrowWhenEmailAndPhoneAreBothNull() {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> User.create(null, null, "Maria Oliveira"));

      assertTrue(ex.getMessage().toLowerCase().contains("email or phone"));
    }

    @Test
    @DisplayName("should throw when full name is null")
    void shouldThrowWhenFullNameIsNull() {
      assertThrows(IllegalArgumentException.class,
          () -> User.create("maria@example.com", null, null));
    }

    @Test
    @DisplayName("should throw when full name is blank")
    void shouldThrowWhenFullNameIsBlank() {
      assertThrows(IllegalArgumentException.class,
          () -> User.create("maria@example.com", null, "   "));
    }

    @Test
    @DisplayName("should split single-word full name correctly")
    void shouldSplitSingleWordFullName() {
      User user = User.create("maria@example.com", null, "Maria");

      assertEquals("Maria", user.getFirstName());
      assertNull(user.getLastName());
    }

    @Test
    @DisplayName("should parse first and last name from full name with multiple words")
    void shouldParseNameWithMultipleWords() {
      User user = User.create("j@example.com", null, "João Carlos da Silva");

      assertEquals("João", user.getFirstName());
      assertEquals("Carlos da Silva", user.getLastName());
    }

    @Test
    @DisplayName("should set default preferred language to pt-BR")
    void shouldSetDefaultPreferredLanguage() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      assertEquals("pt-BR", user.getPreferredLanguage());
    }

    @Test
    @DisplayName("should set default timezone to America/Sao_Paulo")
    void shouldSetDefaultTimezone() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      assertEquals("America/Sao_Paulo", user.getTimezone());
    }

    @Test
    @DisplayName("should set default distance unit to KM")
    void shouldSetDefaultDistanceUnit() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      assertEquals(DistanceUnit.KM, user.getDistanceUnit());
    }
  }

  // ─── Factory restore() ──────────────────────────────────────────

  @Nested
  @DisplayName("User.restore()")
  class RestoreTests {

    @Test
    @DisplayName("should restore user with all fields")
    void shouldRestoreUserWithAllFields() {
      java.util.UUID id = java.util.UUID.randomUUID();
      java.time.Instant now = java.time.Instant.now();

      User user = User.restore(id, "maria@example.com", "+5511999999999",
          "Maria Oliveira", "Maria", "Oliveira",
          LocalDate.of(1990, 5, 15), "F", "http://photo.jpg",
          "pt-BR", "America/Sao_Paulo", DistanceUnit.KM,
          UserStatus.ACTIVE, now, now);

      assertEquals(id, user.getId());
      assertEquals("maria@example.com", user.getEmail());
      assertEquals("+5511999999999", user.getPhoneNumber());
      assertEquals("Maria Oliveira", user.getFullName());
      assertEquals("Maria", user.getFirstName());
      assertEquals("Oliveira", user.getLastName());
      assertEquals(LocalDate.of(1990, 5, 15), user.getBirthDate());
      assertEquals("F", user.getGender());
      assertEquals("http://photo.jpg", user.getProfilePhotoUrl());
      assertEquals("pt-BR", user.getPreferredLanguage());
      assertEquals("America/Sao_Paulo", user.getTimezone());
      assertEquals(DistanceUnit.KM, user.getDistanceUnit());
      assertEquals(UserStatus.ACTIVE, user.getStatus());
      assertEquals(now, user.getCreatedAt());
      assertEquals(now, user.getUpdatedAt());
    }
  }

  // ─── Business methods ───────────────────────────────────────────

  @Nested
  @DisplayName("Business methods")
  class BusinessMethodTests {

    @Test
    @DisplayName("should activate user")
    void shouldActivateUser() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      user.activate();

      assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("should block user")
    void shouldBlockUser() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");
      user.activate();

      user.block();

      assertEquals(UserStatus.BLOCKED, user.getStatus());
    }

    @Test
    @DisplayName("should throw when blocking a pending user")
    void shouldThrowWhenBlockingPendingUser() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      assertThrows(IllegalStateException.class, user::block);
    }

    @Test
    @DisplayName("should throw when activating a blocked user")
    void shouldThrowWhenActivatingBlockedUser() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");
      user.activate();
      user.block();

      assertThrows(IllegalStateException.class, user::activate);
    }
  }

  // ─── updateProfile() ─────────────────────────────────────────────

  @Nested
  @DisplayName("User.updateProfile()")
  class UpdateProfileTests {

    @Test
    @DisplayName("should update all profile fields")
    void shouldUpdateAllProfileFields() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");
      Instant beforeUpdate = user.getUpdatedAt();

      user.updateProfile(
          "Ana Paula Santos", "Ana Paula", "Santos",
          LocalDate.of(1995, 3, 20), "F", "http://new-photo.jpg",
          "en-US", "America/New_York", DistanceUnit.MILES);

      assertEquals("Ana Paula Santos", user.getFullName());
      assertEquals("Ana Paula", user.getFirstName());
      assertEquals("Santos", user.getLastName());
      assertEquals(LocalDate.of(1995, 3, 20), user.getBirthDate());
      assertEquals("F", user.getGender());
      assertEquals("http://new-photo.jpg", user.getProfilePhotoUrl());
      assertEquals("en-US", user.getPreferredLanguage());
      assertEquals("America/New_York", user.getTimezone());
      assertEquals(DistanceUnit.MILES, user.getDistanceUnit());
      assertTrue(user.getUpdatedAt().compareTo(beforeUpdate) >= 0);
    }

    @Test
    @DisplayName("should preserve fields when null is passed")
    void shouldPreserveFieldsWhenNullIsPassed() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      user.updateProfile(null, null, null, null, null, null, null, null, null);

      assertEquals("Maria Oliveira", user.getFullName());
      assertEquals("Maria", user.getFirstName());
      assertNull(user.getBirthDate());
      assertEquals("pt-BR", user.getPreferredLanguage());
      assertEquals("America/Sao_Paulo", user.getTimezone());
      assertEquals(DistanceUnit.KM, user.getDistanceUnit());
    }

    @Test
    @DisplayName("should update only fullName and recalculate firstName/lastName")
    void shouldUpdateOnlyFullNameAndRecalculateNames() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      user.updateProfile("João Carlos da Silva", null, null, null, null, null, null, null, null);

      assertEquals("João Carlos da Silva", user.getFullName());
      assertEquals("João", user.getFirstName());
      assertEquals("Carlos da Silva", user.getLastName());
    }

    @Test
    @DisplayName("should update only firstName and lastName without changing fullName")
    void shouldUpdateOnlyFirstAndLastName() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      user.updateProfile(null, "Ana", "Santos", null, null, null, null, null, null);

      assertEquals("Maria Oliveira", user.getFullName());
      assertEquals("Ana", user.getFirstName());
      assertEquals("Santos", user.getLastName());
    }

    @Test
    @DisplayName("should throw when fullName is blank")
    void shouldThrowWhenFullNameIsBlank() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      assertThrows(IllegalArgumentException.class,
          () -> user.updateProfile("   ", null, null, null, null, null, null, null, null));
    }

    @Test
    @DisplayName("should update single-word fullName correctly")
    void shouldUpdateSingleWordFullName() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      user.updateProfile("Beyoncé", null, null, null, null, null, null, null, null);

      assertEquals("Beyoncé", user.getFullName());
      assertEquals("Beyoncé", user.getFirstName());
      assertNull(user.getLastName());
    }
  }
}
