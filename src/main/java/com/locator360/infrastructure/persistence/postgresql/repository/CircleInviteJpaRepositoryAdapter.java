package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.circle.CircleInvite;
import com.locator360.core.domain.circle.InviteStatus;
import com.locator360.core.port.out.CircleInviteRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.CircleInviteJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CircleInviteJpaRepositoryAdapter implements CircleInviteRepository {

    private final SpringDataCircleInviteRepository springDataCircleInviteRepository;

    @Override
    public CircleInvite save(CircleInvite invite) {
        log.debug("Saving circle invite: {}", invite.getId());
        CircleInviteJpaEntity entity = toJpaEntity(invite);
        CircleInviteJpaEntity savedEntity = springDataCircleInviteRepository.save(entity);
        log.debug("Circle invite saved successfully: {}", savedEntity.getId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<CircleInvite> findByInviteCode(String inviteCode) {
        log.debug("Finding circle invite by code: {}", inviteCode);
        return springDataCircleInviteRepository.findByInviteCode(inviteCode)
                .map(this::toDomain);
    }

    @Override
    public List<CircleInvite> findByCircleId(UUID circleId) {
        log.debug("Finding circle invites by circleId: {}", circleId);
        return springDataCircleInviteRepository.findByCircleId(circleId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CircleInvite> findByCircleIdAndStatus(UUID circleId, InviteStatus status) {
        log.debug("Finding circle invites by circleId: {} and status: {}", circleId, status);
        return springDataCircleInviteRepository.findByCircleIdAndStatus(circleId, status.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CircleInvite toDomain(CircleInviteJpaEntity entity) {
        return CircleInvite.restore(
                entity.getId(),
                entity.getCircleId(),
                entity.getInvitedByUserId(),
                entity.getTargetEmail(),
                entity.getTargetPhone(),
                entity.getInviteCode(),
                InviteStatus.valueOf(entity.getStatus()),
                entity.getAcceptedByUserId(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private CircleInviteJpaEntity toJpaEntity(CircleInvite invite) {
        CircleInviteJpaEntity entity = new CircleInviteJpaEntity();
        entity.setId(invite.getId());
        entity.setCircleId(invite.getCircleId());
        entity.setInvitedByUserId(invite.getInvitedByUserId());
        entity.setTargetEmail(invite.getTargetEmail());
        entity.setTargetPhone(invite.getTargetPhone());
        entity.setInviteCode(invite.getInviteCode());
        entity.setStatus(invite.getStatus().name());
        entity.setAcceptedByUserId(invite.getAcceptedByUserId());
        entity.setExpiresAt(invite.getExpiresAt());
        entity.setCreatedAt(invite.getCreatedAt());
        entity.setUpdatedAt(invite.getUpdatedAt());
        return entity;
    }
}
