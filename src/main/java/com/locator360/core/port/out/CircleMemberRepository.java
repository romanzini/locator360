package com.locator360.core.port.out;

import com.locator360.core.domain.circle.CircleMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CircleMemberRepository {

    CircleMember save(CircleMember member);

    Optional<CircleMember> findByCircleIdAndUserId(UUID circleId, UUID userId);

    List<CircleMember> findByCircleId(UUID circleId);

    List<CircleMember> findByUserId(UUID userId);

    long countByCircleId(UUID circleId);
}
