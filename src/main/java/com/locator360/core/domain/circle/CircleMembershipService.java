package com.locator360.core.domain.circle;

public class CircleMembershipService {

    public static final int DEFAULT_MEMBER_LIMIT = 5;

    public void validateMemberLimit(long currentMemberCount, int memberLimit) {
        if (currentMemberCount >= memberLimit) {
            throw new IllegalStateException(
                    "Circle has reached the maximum number of members (" + memberLimit + ")");
        }
    }

    public void validateMemberLimit(long currentMemberCount) {
        validateMemberLimit(currentMemberCount, DEFAULT_MEMBER_LIMIT);
    }
}
