package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.circle.MemberStatus;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.CircleMemberJpaEntity;
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
public class CircleMemberJpaRepositoryAdapter implements CircleMemberRepository {

    private final SpringDataCircleMemberRepository springDataCircleMemberRepository;

    @Override
    public CircleMember save(CircleMember member) {
        log.debug("Saving circle member: {}", member.getId());
        CircleMemberJpaEntity entity = toJpaEntity(member);
        CircleMemberJpaEntity savedEntity = springDataCircleMemberRepository.save(entity);
        log.debug("Circle member saved successfully: {}", savedEntity.getId());
        return toDomain(savedEntity);
    }

    @Override
    public Optional<CircleMember> findByCircleIdAndUserId(UUID circleId, UUID userId) {
        log.debug("Finding circle member by circleId: {} and userId: {}", circleId, userId);
        return springDataCircleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .map(this::toDomain);
    }

    @Override
    public List<CircleMember> findByCircleId(UUID circleId) {
        log.debug("Finding circle members by circleId: {}", circleId);
        return springDataCircleMemberRepository.findByCircleId(circleId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CircleMember> findByUserId(UUID userId) {
        log.debug("Finding circle members by userId: {}", userId);
        return springDataCircleMemberRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCircleId(UUID circleId) {
        log.debug("Counting circle members for circleId: {}", circleId);
        return springDataCircleMemberRepository.countByCircleId(circleId);
    }

    @Override
    public List<CircleMember> findActiveByCircleId(UUID circleId) {
        log.debug("Finding active circle members for circleId: {}", circleId);
        return springDataCircleMemberRepository.findByCircleIdAndStatus(circleId, MemberStatus.ACTIVE.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CircleMember toDomain(CircleMemberJpaEntity entity) {
        return CircleMember.restore(
                entity.getId(),
                entity.getCircleId(),
                entity.getUserId(),
                CircleRole.valueOf(entity.getRole()),
                MemberStatus.valueOf(entity.getStatus()),
                entity.getJoinedAt(),
                entity.getLeftAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private CircleMemberJpaEntity toJpaEntity(CircleMember member) {
        CircleMemberJpaEntity entity = new CircleMemberJpaEntity();
        entity.setId(member.getId());
        entity.setCircleId(member.getCircleId());
        entity.setUserId(member.getUserId());
        entity.setRole(member.getRole().name());
        entity.setStatus(member.getStatus().name());
        entity.setJoinedAt(member.getJoinedAt());
        entity.setLeftAt(member.getLeftAt());
        entity.setCreatedAt(member.getCreatedAt());
        entity.setUpdatedAt(member.getUpdatedAt());
        return entity;
    }
}
