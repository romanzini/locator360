package com.locator360.core.domain.circle;

import java.time.Instant;
import java.util.UUID;

public class CircleInvite {

    private final UUID id;
    private final UUID circleId;
    private final UUID invitedByUserId;
    private final String targetEmail;
    private final String targetPhone;
    private final String inviteCode;
    private InviteStatus status;
    private UUID acceptedByUserId;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private CircleInvite(UUID id, UUID circleId, UUID invitedByUserId,
                         String targetEmail, String targetPhone, String inviteCode,
                         InviteStatus status, UUID acceptedByUserId, Instant expiresAt,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.circleId = circleId;
        this.invitedByUserId = invitedByUserId;
        this.targetEmail = targetEmail;
        this.targetPhone = targetPhone;
        this.inviteCode = inviteCode;
        this.status = status;
        this.acceptedByUserId = acceptedByUserId;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─── Factory: criação ───────────────────────────────────────────

    public static CircleInvite create(UUID circleId, UUID invitedByUserId,
                                      String targetEmail, String targetPhone,
                                      Instant expiresAt) {
        validateCreation(circleId, invitedByUserId);
        Instant now = Instant.now();
        String code = generateInviteCode();
        return new CircleInvite(UUID.randomUUID(), circleId, invitedByUserId,
                targetEmail, targetPhone, code, InviteStatus.PENDING,
                null, expiresAt, now, now);
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static CircleInvite restore(UUID id, UUID circleId, UUID invitedByUserId,
                                       String targetEmail, String targetPhone,
                                       String inviteCode, InviteStatus status,
                                       UUID acceptedByUserId, Instant expiresAt,
                                       Instant createdAt, Instant updatedAt) {
        return new CircleInvite(id, circleId, invitedByUserId, targetEmail, targetPhone,
                inviteCode, status, acceptedByUserId, expiresAt, createdAt, updatedAt);
    }

    // ─── Business methods ───────────────────────────────────────────

    public void accept(UUID userId) {
        if (this.status != InviteStatus.PENDING) {
            throw new IllegalStateException("Only pending invites can be accepted");
        }
        if (isExpired()) {
            throw new IllegalStateException("Invite has expired");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to accept invite");
        }
        this.status = InviteStatus.ACCEPTED;
        this.acceptedByUserId = userId;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status != InviteStatus.PENDING) {
            throw new IllegalStateException("Only pending invites can be cancelled");
        }
        this.status = InviteStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void expire() {
        if (this.status != InviteStatus.PENDING) {
            throw new IllegalStateException("Only pending invites can be expired");
        }
        this.status = InviteStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public boolean isExpired() {
        return this.expiresAt != null && Instant.now().isAfter(this.expiresAt);
    }

    public boolean isPending() {
        return this.status == InviteStatus.PENDING;
    }

    // ─── Validations ────────────────────────────────────────────────

    private static void validateCreation(UUID circleId, UUID invitedByUserId) {
        if (circleId == null) {
            throw new IllegalArgumentException("Circle ID is required");
        }
        if (invitedByUserId == null) {
            throw new IllegalArgumentException("Invited by user ID is required");
        }
    }

    // ─── Invite code generation ─────────────────────────────────────

    private static String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    // ─── Getters ────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getCircleId() {
        return circleId;
    }

    public UUID getInvitedByUserId() {
        return invitedByUserId;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public String getTargetPhone() {
        return targetPhone;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public UUID getAcceptedByUserId() {
        return acceptedByUserId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
