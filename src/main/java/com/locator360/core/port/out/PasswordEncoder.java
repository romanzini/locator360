package com.locator360.core.port.out;

public interface PasswordEncoder {

  String encode(String rawPassword);

  boolean matches(String rawPassword, String encodedPassword);
}
