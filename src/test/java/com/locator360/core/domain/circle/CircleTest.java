package com.locator360.core.domain.circle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CircleTest {

    // ─── Factory create() ───────────────────────────────────────────

    @Nested
    @DisplayName("Circle.create()")
    class CreateTests {

        @Test
        @DisplayName("should create circle with all fields")
        void shouldCreateCircleWithAllFields() {
            UUID userId = UUID.randomUUID();

            Circle circle = Circle.create("Família Silva", "Grupo da família",
                    "http://photo.jpg", "#4CAF50", PrivacyLevel.INVITE_ONLY, userId);

            assertNotNull(circle.getId());
            assertEquals("Família Silva", circle.getName());
            assertEquals("Grupo da família", circle.getDescription());
            assertEquals("http://photo.jpg", circle.getPhotoUrl());
            assertEquals("#4CAF50", circle.getColorHex());
            assertEquals(PrivacyLevel.INVITE_ONLY, circle.getPrivacyLevel());
            assertEquals(userId, circle.getCreatedByUserId());
            assertNotNull(circle.getCreatedAt());
            assertNotNull(circle.getUpdatedAt());
        }

        @Test
        @DisplayName("should create circle with only required fields")
        void shouldCreateCircleWithOnlyRequiredFields() {
            UUID userId = UUID.randomUUID();

            Circle circle = Circle.create("Família", null, null, null, null, userId);

            assertNotNull(circle.getId());
            assertEquals("Família", circle.getName());
            assertNull(circle.getDescription());
            assertNull(circle.getPhotoUrl());
            assertNull(circle.getColorHex());
            assertEquals(PrivacyLevel.OPEN_WITH_CODE, circle.getPrivacyLevel());
            assertEquals(userId, circle.getCreatedByUserId());
        }

        @Test
        @DisplayName("should default privacy level to OPEN_WITH_CODE")
        void shouldDefaultPrivacyLevel() {
            UUID userId = UUID.randomUUID();

            Circle circle = Circle.create("Família", null, null, null, null, userId);

            assertEquals(PrivacyLevel.OPEN_WITH_CODE, circle.getPrivacyLevel());
        }

        @Test
        @DisplayName("should trim circle name")
        void shouldTrimCircleName() {
            UUID userId = UUID.randomUUID();

            Circle circle = Circle.create("  Família Silva  ", null, null, null, null, userId);

            assertEquals("Família Silva", circle.getName());
        }

        @Test
        @DisplayName("should throw when name is null")
        void shouldThrowWhenNameIsNull() {
            UUID userId = UUID.randomUUID();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> Circle.create(null, null, null, null, null, userId));

            assertTrue(ex.getMessage().toLowerCase().contains("name"));
        }

        @Test
        @DisplayName("should throw when name is blank")
        void shouldThrowWhenNameIsBlank() {
            UUID userId = UUID.randomUUID();

            assertThrows(IllegalArgumentException.class,
                    () -> Circle.create("   ", null, null, null, null, userId));
        }

        @Test
        @DisplayName("should throw when creator user ID is null")
        void shouldThrowWhenCreatorUserIdIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> Circle.create("Família", null, null, null, null, null));

            assertTrue(ex.getMessage().toLowerCase().contains("creator"));
        }
    }

    // ─── Factory restore() ──────────────────────────────────────────

    @Nested
    @DisplayName("Circle.restore()")
    class RestoreTests {

        @Test
        @DisplayName("should restore circle with all fields")
        void shouldRestoreCircleWithAllFields() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();

            Circle circle = Circle.restore(id, "Família", "Descrição", "http://photo.jpg",
                    "#FF6600", PrivacyLevel.INVITE_ONLY, userId, now, now, null);

            assertEquals(id, circle.getId());
            assertEquals("Família", circle.getName());
            assertEquals("Descrição", circle.getDescription());
            assertEquals("http://photo.jpg", circle.getPhotoUrl());
            assertEquals("#FF6600", circle.getColorHex());
            assertEquals(PrivacyLevel.INVITE_ONLY, circle.getPrivacyLevel());
            assertEquals(userId, circle.getCreatedByUserId());
            assertEquals(now, circle.getCreatedAt());
            assertEquals(now, circle.getUpdatedAt());
            assertNull(circle.getDeletedAt());
        }

        @Test
        @DisplayName("should restore circle with deletedAt")
        void shouldRestoreCircleWithDeletedAt() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant deletedAt = Instant.now();

            Circle circle = Circle.restore(id, "Família", "Descrição", "http://photo.jpg",
                    "#FF6600", PrivacyLevel.INVITE_ONLY, userId, now, now, deletedAt);

            assertEquals(deletedAt, circle.getDeletedAt());
            assertTrue(circle.isDeleted());
        }
    }

    // ─── Business methods ───────────────────────────────────────────

    @Nested
    @DisplayName("Circle.delete()")
    class DeleteTests {

        @Test
        @DisplayName("should soft-delete circle")
        void shouldSoftDeleteCircle() {
            UUID userId = UUID.randomUUID();
            Circle circle = Circle.create("Família", null, null, null, null, userId);

            assertFalse(circle.isDeleted());
            assertNull(circle.getDeletedAt());

            circle.delete();

            assertTrue(circle.isDeleted());
            assertNotNull(circle.getDeletedAt());
            assertNotNull(circle.getUpdatedAt());
        }

        @Test
        @DisplayName("should throw when deleting already deleted circle")
        void shouldThrowWhenDeletingAlreadyDeletedCircle() {
            UUID userId = UUID.randomUUID();
            Circle circle = Circle.create("Família", null, null, null, null, userId);

            circle.delete();

            assertThrows(IllegalStateException.class, () -> circle.delete());
        }
    }

    @Nested
    @DisplayName("Circle.update()")
    class UpdateTests {

        @Test
        @DisplayName("should update all fields")
        void shouldUpdateAllFields() {
            UUID userId = UUID.randomUUID();
            Circle circle = Circle.create("Original", null, null, null, null, userId);

            circle.update("Novo Nome", "Nova Descrição", "http://new.jpg",
                    "#AABBCC", PrivacyLevel.INVITE_ONLY);

            assertEquals("Novo Nome", circle.getName());
            assertEquals("Nova Descrição", circle.getDescription());
            assertEquals("http://new.jpg", circle.getPhotoUrl());
            assertEquals("#AABBCC", circle.getColorHex());
            assertEquals(PrivacyLevel.INVITE_ONLY, circle.getPrivacyLevel());
        }

        @Test
        @DisplayName("should update only non-null fields")
        void shouldUpdateOnlyNonNullFields() {
            UUID userId = UUID.randomUUID();
            Circle circle = Circle.create("Original", "Desc", "http://old.jpg",
                    "#000000", PrivacyLevel.OPEN_WITH_CODE, userId);

            circle.update("Novo Nome", null, null, null, null);

            assertEquals("Novo Nome", circle.getName());
            assertEquals("Desc", circle.getDescription());
            assertEquals("http://old.jpg", circle.getPhotoUrl());
            assertEquals("#000000", circle.getColorHex());
            assertEquals(PrivacyLevel.OPEN_WITH_CODE, circle.getPrivacyLevel());
        }

        @Test
        @DisplayName("should throw when updating name to blank")
        void shouldThrowWhenUpdatingNameToBlank() {
            UUID userId = UUID.randomUUID();
            Circle circle = Circle.create("Original", null, null, null, null, userId);

            assertThrows(IllegalArgumentException.class,
                    () -> circle.update("   ", null, null, null, null));
        }

        @Test
        @DisplayName("should update updatedAt timestamp")
        void shouldUpdateUpdatedAtTimestamp() {
            UUID userId = UUID.randomUUID();
            Circle circle = Circle.create("Original", null, null, null, null, userId);
            Instant before = circle.getUpdatedAt();

            circle.update("Novo Nome", null, null, null, null);

            assertTrue(circle.getUpdatedAt().compareTo(before) >= 0);
        }
    }
}
