package com.locator360.api.rest.circle;

import com.locator360.core.port.in.circle.CreateCircleUseCase;
import com.locator360.core.port.in.circle.CreateInviteUseCase;
import com.locator360.core.port.in.circle.JoinCircleUseCase;
import com.locator360.core.port.in.dto.input.CreateCircleInputDto;
import com.locator360.core.port.in.dto.input.CreateInviteInputDto;
import com.locator360.core.port.in.dto.input.JoinCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;
import com.locator360.core.port.in.dto.output.CircleOutputDto;
import com.locator360.core.port.in.dto.output.InviteOutputDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/circles")
@RequiredArgsConstructor
@Slf4j
public class CircleController implements CircleControllerApi {

    private final CreateCircleUseCase createCircleUseCase;
    private final CreateInviteUseCase createInviteUseCase;
    private final JoinCircleUseCase joinCircleUseCase;

    @Override
    public ResponseEntity<CircleOutputDto> create(@Valid @RequestBody CreateCircleInputDto input) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Received create circle request from user: {}", userId);

        CircleOutputDto output = createCircleUseCase.execute(userId, input);
        log.info("Circle created: {}", output.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @Override
    public ResponseEntity<InviteOutputDto> createInvite(@PathVariable UUID circleId,
                                                        @Valid @RequestBody CreateInviteInputDto input) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Received create invite request for circle: {} from user: {}", circleId, userId);

        InviteOutputDto output = createInviteUseCase.execute(userId, circleId, input);
        log.info("Invite created for circle: {} with code: {}", circleId, output.getInviteCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @Override
    public ResponseEntity<CircleMemberOutputDto> join(@Valid @RequestBody JoinCircleInputDto input) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("Received join circle request from user: {} with code: {}", userId, input.getInviteCode());

        CircleMemberOutputDto output = joinCircleUseCase.execute(userId, input);
        log.info("User: {} joined circle: {}", userId, output.getCircleId());

        return ResponseEntity.ok(output);
    }
}
