package com.locator360.api.rest.location;

import com.locator360.core.port.in.dto.input.StreamLocationInputDto;
import com.locator360.core.port.in.location.StreamLocationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController implements LocationControllerApi {

    private final StreamLocationUseCase streamLocationUseCase;

    @Override
    public ResponseEntity<Void> stream(@Valid @RequestBody StreamLocationInputDto input) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Received location stream request from user: {} with {} events", userId, input.getEvents().size());
        streamLocationUseCase.execute(userId, input);
        log.info("Location stream accepted for user: {} with {} events", userId, input.getEvents().size());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
