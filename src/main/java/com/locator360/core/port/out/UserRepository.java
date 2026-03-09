package com.locator360.core.port.out;

import com.locator360.core.domain.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  User save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(String email);

  Optional<User> findByPhoneNumber(String phoneNumber);

  boolean existsByEmail(String email);

  boolean existsByPhoneNumber(String phoneNumber);
}
