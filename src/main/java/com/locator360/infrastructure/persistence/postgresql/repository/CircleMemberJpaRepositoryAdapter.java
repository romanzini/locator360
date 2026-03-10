package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.CircleMemberJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    @Override
    public CircleMember save(CircleMember member) {
        log.debug("Saving circle member: {}", member.getId());
        CircleMemberJpaEntity entity = modelMapper.map(member, CircleMemberJpaEntity.class);
        CircleMemberJpaEntity savedEntity = springDataCircleMemberRepository.save(entity);
        return modelMapper.map(savedEntity, CircleMember.class);
    }

    @Override
    public Optional<CircleMember> findByCircleIdAndUserId(UUID circleId, UUID userId) {
        log.debug("Finding circle member by circleId: {} and userId: {}", circleId, userId);
        return springDataCircleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .map(entity -> modelMapper.map(entity, CircleMember.class));
    }

    @Override
    public List<CircleMember> findByCircleId(UUID circleId) {
        log.debug("Finding circle members by circleId: {}", circleId);
        return springDataCircleMemberRepository.findByCircleId(circleId).stream()
                .map(entity -> modelMapper.map(entity, CircleMember.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CircleMember> findByUserId(UUID userId) {
        log.debug("Finding circle members by userId: {}", userId);
        return springDataCircleMemberRepository.findByUserId(userId).stream()
                .map(entity -> modelMapper.map(entity, CircleMember.class))
                .collect(Collectors.toList());
    }

    @Override
    public long countByCircleId(UUID circleId) {
        log.debug("Counting circle members for circleId: {}", circleId);
        return springDataCircleMemberRepository.countByCircleId(circleId);
    }
}
