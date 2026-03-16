package com.locator360.core.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LocationConfig {

    @Bean
    public Duration staleThreshold(
            @Value("${locator360.location.stale-threshold-minutes:5}") long minutes) {
        return Duration.ofMinutes(minutes);
    }
}
