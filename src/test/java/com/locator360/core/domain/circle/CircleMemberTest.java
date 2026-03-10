package com.locator360.core.domain.circle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CircleMemberTest {

    // ─── Factory createAdmin() ──────────────────────────────────────

    @Nested
    @DisplayName("CircleMember.createAdmin()")
    class CreateAdminTests {

        @Test
        @DisplayName("should create admin member with correct defaults")
        void shouldCreateAdminMember() {
            UUID circleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            CircleMember member = CircleMember.createAdmin(circleId, userId);

            assertNotNull(member.getId());
            assertEquals(circleId, member.getCircleId());
            assertEquals(userId, member.getUserId());
            assertEquals(CircleRole.ADMIN, member.getRole());
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
            assertNotNull(member.getJoinedAt());
            assertNull(member.getLeftAt());
            assertNotNull(member.getCreatedAt());
            assertNotNull(member.getUpdatedAt());
        }

        @Test
        @DisplayName("should throw when circle ID is null")
        void shouldThrowWhenCircleIdIsNull() {
            UUID userId = UUID.randomUUID();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> CircleMember.createAdmin(null, userId));

            assertTrue(ex.getMessage().toLowerCase().contains("circle id"));
        }

        @Test
        @DisplayName("should throw when user ID is null")
        void shouldThrowWhenUserIdIsNull() {
            UUID circleId = UUID.randomUUID();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> CircleMember.createAdmin(circleId, null));

            assertTrue(ex.getMessage().toLowerCase().contains("user id"));
        }
    }

    // ─── Factory createMember() ─────────────────────────────────────

    @Nested
    @DisplayName("CircleMember.createMember()")
    class CreateMemberTests {

        @Test
        @DisplayName("should create regular member with MEMBER role")
        void shouldCreateRegularMember() {
            UUID circleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            CircleMember member = CircleMember.createMember(circleId, userId);

            assertEquals(CircleRole.MEMBER, member.getRole());
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
            assertNotNull(member.getJoinedAt());
        }
    }

    // ─── Factory restore() ──────────────────────────────────────────

    @Nested
    @DisplayName("CircleMember.restore()")
    class RestoreTests {

        @Test
        @DisplayName("should restore member with all fields")
        void shouldRestoreMemberWithAllFields() {
            UUID id = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();

            CircleMember member = CircleMember.restore(id, circleId, userId,
                    CircleRole.ADMIN, MemberStatus.ACTIVE, now, null, now, now);

            assertEquals(id, member.getId());
            assertEquals(circleId, member.getCircleId());
            assertEquals(userId, member.getUserId());
            assertEquals(CircleRole.ADMIN, member.getRole());
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
            assertEquals(now, member.getJoinedAt());
            assertNull(member.getLeftAt());
        }
    }

    // ─── Business methods ───────────────────────────────────────────

    @Nested
    @DisplayName("Business methods")
    class BusinessMethodTests {

        @Test
        @DisplayName("should remove active member")
        void shouldRemoveActiveMember() {
            CircleMember member = CircleMember.createMember(UUID.randomUUID(), UUID.randomUUID());

            member.remove();

            assertEquals(MemberStatus.REMOVED, member.getStatus());
            assertNotNull(member.getLeftAt());
        }

        @Test
        @DisplayName("should throw when removing non-active member")
        void shouldThrowWhenRemovingNonActiveMember() {
            CircleMember member = CircleMember.createMember(UUID.randomUUID(), UUID.randomUUID());
            member.remove();

            assertThrows(IllegalStateException.class, member::remove);
        }

        @Test
        @DisplayName("should promote member to admin")
        void shouldPromoteToAdmin() {
            CircleMember member = CircleMember.createMember(UUID.randomUUID(), UUID.randomUUID());

            member.promoteToAdmin();

            assertEquals(CircleRole.ADMIN, member.getRole());
        }

        @Test
        @DisplayName("should throw when promoting non-active member")
        void shouldThrowWhenPromotingNonActiveMember() {
            CircleMember member = CircleMember.createMember(UUID.randomUUID(), UUID.randomUUID());
            member.remove();

            assertThrows(IllegalStateException.class, member::promoteToAdmin);
        }

        @Test
        @DisplayName("isAdmin should return true for active admin")
        void isAdminShouldReturnTrueForActiveAdmin() {
            CircleMember member = CircleMember.createAdmin(UUID.randomUUID(), UUID.randomUUID());

            assertTrue(member.isAdmin());
        }

        @Test
        @DisplayName("isAdmin should return false for removed admin")
        void isAdminShouldReturnFalseForRemovedAdmin() {
            CircleMember member = CircleMember.createAdmin(UUID.randomUUID(), UUID.randomUUID());
            member.remove();

            assertFalse(member.isAdmin());
        }

        @Test
        @DisplayName("isActive should return true for active member")
        void isActiveShouldReturnTrue() {
            CircleMember member = CircleMember.createMember(UUID.randomUUID(), UUID.randomUUID());

            assertTrue(member.isActive());
        }

        @Test
        @DisplayName("isActive should return false for removed member")
        void isActiveShouldReturnFalse() {
            CircleMember member = CircleMember.createMember(UUID.randomUUID(), UUID.randomUUID());
            member.remove();

            assertFalse(member.isActive());
        }
    }
}
