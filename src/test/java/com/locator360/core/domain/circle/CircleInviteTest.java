package com.locator360.core.domain.circle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CircleInviteTest {

    // ─── Factory create() ───────────────────────────────────────────

    @Nested
    @DisplayName("CircleInvite.create()")
    class CreateTests {

        @Test
        @DisplayName("should create invite with correct defaults")
        void shouldCreateInviteWithDefaults() {
            UUID circleId = UUID.randomUUID();
            UUID invitedByUserId = UUID.randomUUID();

            CircleInvite invite = CircleInvite.create(circleId, invitedByUserId,
                    "test@email.com", null, null);

            assertNotNull(invite.getId());
            assertEquals(circleId, invite.getCircleId());
            assertEquals(invitedByUserId, invite.getInvitedByUserId());
            assertEquals("test@email.com", invite.getTargetEmail());
            assertNull(invite.getTargetPhone());
            assertNotNull(invite.getInviteCode());
            assertEquals(8, invite.getInviteCode().length());
            assertEquals(InviteStatus.PENDING, invite.getStatus());
            assertNull(invite.getAcceptedByUserId());
            assertNull(invite.getExpiresAt());
            assertNotNull(invite.getCreatedAt());
            assertNotNull(invite.getUpdatedAt());
        }

        @Test
        @DisplayName("should create invite with phone number")
        void shouldCreateInviteWithPhone() {
            UUID circleId = UUID.randomUUID();
            UUID invitedByUserId = UUID.randomUUID();

            CircleInvite invite = CircleInvite.create(circleId, invitedByUserId,
                    null, "+5511999999999", null);

            assertNull(invite.getTargetEmail());
            assertEquals("+5511999999999", invite.getTargetPhone());
        }

        @Test
        @DisplayName("should create invite with expiration date")
        void shouldCreateInviteWithExpiration() {
            UUID circleId = UUID.randomUUID();
            UUID invitedByUserId = UUID.randomUUID();
            Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

            CircleInvite invite = CircleInvite.create(circleId, invitedByUserId,
                    null, null, expiresAt);

            assertEquals(expiresAt, invite.getExpiresAt());
        }

        @Test
        @DisplayName("should generate unique invite codes")
        void shouldGenerateUniqueInviteCodes() {
            UUID circleId = UUID.randomUUID();
            UUID invitedByUserId = UUID.randomUUID();

            CircleInvite invite1 = CircleInvite.create(circleId, invitedByUserId, null, null, null);
            CircleInvite invite2 = CircleInvite.create(circleId, invitedByUserId, null, null, null);

            assertNotEquals(invite1.getInviteCode(), invite2.getInviteCode());
        }

        @Test
        @DisplayName("should throw when circle ID is null")
        void shouldThrowWhenCircleIdIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> CircleInvite.create(null, UUID.randomUUID(), null, null, null));

            assertTrue(ex.getMessage().toLowerCase().contains("circle id"));
        }

        @Test
        @DisplayName("should throw when invited by user ID is null")
        void shouldThrowWhenInvitedByUserIdIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> CircleInvite.create(UUID.randomUUID(), null, null, null, null));

            assertTrue(ex.getMessage().toLowerCase().contains("invited by user id"));
        }
    }

    // ─── Factory restore() ──────────────────────────────────────────

    @Nested
    @DisplayName("CircleInvite.restore()")
    class RestoreTests {

        @Test
        @DisplayName("should restore invite with all fields")
        void shouldRestoreInviteWithAllFields() {
            UUID id = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();
            UUID invitedByUserId = UUID.randomUUID();
            UUID acceptedByUserId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant expiresAt = now.plus(7, ChronoUnit.DAYS);

            CircleInvite invite = CircleInvite.restore(id, circleId, invitedByUserId,
                    "test@email.com", "+5511999999999", "ABC12345",
                    InviteStatus.ACCEPTED, acceptedByUserId, expiresAt, now, now);

            assertEquals(id, invite.getId());
            assertEquals(circleId, invite.getCircleId());
            assertEquals(invitedByUserId, invite.getInvitedByUserId());
            assertEquals("test@email.com", invite.getTargetEmail());
            assertEquals("+5511999999999", invite.getTargetPhone());
            assertEquals("ABC12345", invite.getInviteCode());
            assertEquals(InviteStatus.ACCEPTED, invite.getStatus());
            assertEquals(acceptedByUserId, invite.getAcceptedByUserId());
            assertEquals(expiresAt, invite.getExpiresAt());
        }
    }

    // ─── Business methods ───────────────────────────────────────────

    @Nested
    @DisplayName("accept()")
    class AcceptTests {

        @Test
        @DisplayName("should accept pending invite")
        void shouldAcceptPendingInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);
            UUID acceptingUserId = UUID.randomUUID();

            invite.accept(acceptingUserId);

            assertEquals(InviteStatus.ACCEPTED, invite.getStatus());
            assertEquals(acceptingUserId, invite.getAcceptedByUserId());
        }

        @Test
        @DisplayName("should throw when accepting non-pending invite")
        void shouldThrowWhenAcceptingNonPendingInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);
            invite.cancel();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> invite.accept(UUID.randomUUID()));

            assertTrue(ex.getMessage().toLowerCase().contains("pending"));
        }

        @Test
        @DisplayName("should throw when accepting expired invite")
        void shouldThrowWhenAcceptingExpiredInvite() {
            Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, pastDate);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> invite.accept(UUID.randomUUID()));

            assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        }

        @Test
        @DisplayName("should throw when user ID is null")
        void shouldThrowWhenUserIdIsNull() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> invite.accept(null));

            assertTrue(ex.getMessage().toLowerCase().contains("user id"));
        }
    }

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("should cancel pending invite")
        void shouldCancelPendingInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);

            invite.cancel();

            assertEquals(InviteStatus.CANCELLED, invite.getStatus());
        }

        @Test
        @DisplayName("should throw when cancelling non-pending invite")
        void shouldThrowWhenCancellingNonPendingInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);
            invite.accept(UUID.randomUUID());

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    invite::cancel);

            assertTrue(ex.getMessage().toLowerCase().contains("pending"));
        }
    }

    @Nested
    @DisplayName("expire()")
    class ExpireTests {

        @Test
        @DisplayName("should expire pending invite")
        void shouldExpirePendingInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);

            invite.expire();

            assertEquals(InviteStatus.EXPIRED, invite.getStatus());
        }

        @Test
        @DisplayName("should throw when expiring non-pending invite")
        void shouldThrowWhenExpiringNonPendingInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);
            invite.accept(UUID.randomUUID());

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    invite::expire);

            assertTrue(ex.getMessage().toLowerCase().contains("pending"));
        }
    }

    @Nested
    @DisplayName("isExpired()")
    class IsExpiredTests {

        @Test
        @DisplayName("should return true when past expiration date")
        void shouldReturnTrueWhenPastExpiration() {
            Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, pastDate);

            assertTrue(invite.isExpired());
        }

        @Test
        @DisplayName("should return false when before expiration date")
        void shouldReturnFalseWhenBeforeExpiration() {
            Instant futureDate = Instant.now().plus(7, ChronoUnit.DAYS);
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, futureDate);

            assertFalse(invite.isExpired());
        }

        @Test
        @DisplayName("should return false when no expiration date")
        void shouldReturnFalseWhenNoExpiration() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);

            assertFalse(invite.isExpired());
        }
    }

    @Nested
    @DisplayName("isPending()")
    class IsPendingTests {

        @Test
        @DisplayName("should return true for pending invite")
        void shouldReturnTrueForPendingInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);

            assertTrue(invite.isPending());
        }

        @Test
        @DisplayName("should return false for accepted invite")
        void shouldReturnFalseForAcceptedInvite() {
            CircleInvite invite = CircleInvite.create(UUID.randomUUID(), UUID.randomUUID(),
                    null, null, null);
            invite.accept(UUID.randomUUID());

            assertFalse(invite.isPending());
        }
    }
}
