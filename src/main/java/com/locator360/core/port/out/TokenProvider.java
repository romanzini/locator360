package com.locator360.core.port.out;

import java.util.UUID;

public interface TokenProvider {

  String generateAccessToken(UUID userId);

  String generateRefreshToken(UUID userId);

  UUID validateToken(String token);

  long getAccessTokenExpirationSeconds();
}
