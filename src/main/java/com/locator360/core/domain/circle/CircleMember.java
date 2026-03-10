package com.locator360.core.domain.circle;

import java.time.Instant;
import java.util.UUID;

public class CircleMember {

    private final UUID id;
    private final UUID circleId;
    private final UUID userId;
    private CircleRole role;
    private MemberStatus status;
    private Instant joinedAt;
    private Instant leftAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private CircleMember(UUID id, UUID circleId, UUID userId, CircleRole role,
                         MemberStatus status, Instant joinedAt, Instant leftAt,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.circleId = circleId;
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─── Factory: criação como admin ────────────────────────────────

    public static CircleMember createAdmin(UUID circleId, UUID userId) {
        validateCreation(circleId, userId);
        Instant now = Instant.now();
        return new CircleMember(UUID.randomUUID(), circleId, userId,
                CircleRole.ADMIN, MemberStatus.ACTIVE, now, null, now, now);
    }

    // ─── Factory: criação como membro ───────────────────────────────

    public static CircleMember createMember(UUID circleId, UUID userId) {
        validateCreation(circleId, userId);
        Instant now = Instant.now();
        return new CircleMember(UUID.randomUUID(), circleId, userId,
                CircleRole.MEMBER, MemberStatus.ACTIVE, now, null, now, now);
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static CircleMember restore(UUID id, UUID circleId, UUID userId, CircleRole role,
                                       MemberStatus status, Instant joinedAt, Instant leftAt,
                                       Instant createdAt, Instant updatedAt) {
        return new CircleMember(id, circleId, userId, role, status,
                joinedAt, leftAt, createdAt, updatedAt);
    }

    // ─── Business methods ───────────────────────────────────────────

    public void remove() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("Only active members can be removed");
        }
        this.status = MemberStatus.REMOVED;
        this.leftAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void promoteToAdmin() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("Only active members can be promoted");
        }
        this.role = CircleRole.ADMIN;
        this.updatedAt = Instant.now();
    }

    public boolean isAdmin() {
        return this.role == CircleRole.ADMIN && this.status == MemberStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    // ─── Validations ────────────────────────────────────────────────

    private static void validateCreation(UUID circleId, UUID userId) {
        if (circleId == null) {
            throw new IllegalArgumentException("Circle ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

    // ─── Getters ────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getCircleId() {
        return circleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public CircleRole getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
