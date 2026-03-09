package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.VerificationToken;
import com.locator360.core.port.out.VerificationTokenRepository;
import com.locator360.infrastructure.persistence.postgresql.entity.VerificationTokenJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class VerificationTokenJpaRepositoryAdapter implements VerificationTokenRepository {

    private final SpringDataVerificationTokenRepository springDataVerificationTokenRepository;
    private final ModelMapper modelMapper;

    @Override
    public VerificationToken save(VerificationToken verificationToken) {
        log.debug("Saving verification token for user: {}", verificationToken.getUserId());
        VerificationTokenJpaEntity entity =
                modelMapper.map(verificationToken, VerificationTokenJpaEntity.class);
        VerificationTokenJpaEntity savedEntity =
                springDataVerificationTokenRepository.save(entity);
        return modelMapper.map(savedEntity, VerificationToken.class);
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        log.debug("Finding verification token by token string");
        return springDataVerificationTokenRepository.findByToken(token)
                .map(entity -> modelMapper.map(entity, VerificationToken.class));
    }

    @Override
    public Optional<VerificationToken> findByUserIdAndType(UUID userId, TokenType type) {
        log.debug("Finding verification token by userId: {} and type: {}", userId, type);
        return springDataVerificationTokenRepository
                .findByUserIdAndType(userId, type.name())
                .map(entity -> modelMapper.map(entity, VerificationToken.class));
    }
}
