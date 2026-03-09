package com.familylocator.core.port.out;

import com.familylocator.core.domain.user.TokenType;
import com.familylocator.core.domain.user.VerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository {

  VerificationToken save(VerificationToken verificationToken);

  Optional<VerificationToken> findByToken(String token);

  Optional<VerificationToken> findByUserIdAndType(UUID userId, TokenType type);
}
