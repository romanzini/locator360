package com.locator360.core.port.out;

import com.locator360.core.domain.circle.CircleInvite;
import com.locator360.core.domain.circle.InviteStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CircleInviteRepository {

    CircleInvite save(CircleInvite invite);

    Optional<CircleInvite> findByInviteCode(String inviteCode);

    List<CircleInvite> findByCircleId(UUID circleId);

    List<CircleInvite> findByCircleIdAndStatus(UUID circleId, InviteStatus status);
}
