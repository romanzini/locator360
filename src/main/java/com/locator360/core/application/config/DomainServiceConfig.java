package com.locator360.core.application.config;

import com.locator360.core.domain.service.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

  @Bean
  public AuthenticationService authenticationService() {
    return new AuthenticationService();
  }
}
