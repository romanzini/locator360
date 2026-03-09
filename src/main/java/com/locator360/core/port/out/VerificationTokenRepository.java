package com.locator360.core.port.out;

import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.VerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository {

  VerificationToken save(VerificationToken verificationToken);

  Optional<VerificationToken> findByToken(String token);

  Optional<VerificationToken> findByUserIdAndType(UUID userId, TokenType type);
}
