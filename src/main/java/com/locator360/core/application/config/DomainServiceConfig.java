package com.locator360.core.application.config;

import com.locator360.core.domain.circle.CircleMembershipService;
import com.locator360.core.domain.service.AuthenticationService;
import com.locator360.core.domain.service.GeofenceDetectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

  @Bean
  public AuthenticationService authenticationService() {
    return new AuthenticationService();
  }

  @Bean
  public CircleMembershipService circleMembershipService() {
    return new CircleMembershipService();
  }

  @Bean
  public GeofenceDetectionService geofenceDetectionService() {
    return new GeofenceDetectionService();
  }
}
