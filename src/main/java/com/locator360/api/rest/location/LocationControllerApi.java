package com.locator360.api.rest.location;

import com.locator360.core.port.in.dto.input.PauseLocationInputDto;
import com.locator360.core.port.in.dto.input.StreamLocationInputDto;
import com.locator360.core.port.in.dto.output.MemberLocationOutputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Locations", description = "Rastreamento de localização em tempo real")
public interface LocationControllerApi {

        @Operation(summary = "Enviar lote de eventos de localização", description = "Endpoint usado pelo app mobile para enviar eventos de localização em lote")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Eventos aceitos para processamento"),
                        @ApiResponse(responseCode = "400", description = "Payload inválido"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado")
        })
        @PostMapping("/api/v1/locations/stream")
        ResponseEntity<Void> stream(@Valid @RequestBody StreamLocationInputDto input);

        @Operation(summary = "Obter localização de todos os membros do círculo", description = "Retorna a última localização conhecida de cada membro ativo que está compartilhando localização")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de localizações dos membros"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "422", description = "Usuário não é membro do círculo")
        })
        @GetMapping("/api/v1/circles/{circleId}/members/locations")
        ResponseEntity<List<MemberLocationOutputDto>> getCircleMembersLocation(
                        @Parameter(description = "ID do círculo") @PathVariable UUID circleId);

        @Operation(summary = "Pausar compartilhamento de localização", description = "Pausa o compartilhamento de localização do usuário autenticado no círculo informado")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Pausa aplicada com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Payload inválido"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "422", description = "Usuário não é membro ativo do círculo")
        })
        @PostMapping("/api/v1/circles/{circleId}/location-sharing/pause")
        ResponseEntity<Void> pauseLocationSharing(
                        @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
                        @Valid @RequestBody PauseLocationInputDto input);

        @Operation(summary = "Retomar compartilhamento de localização", description = "Retoma o compartilhamento de localização do usuário autenticado no círculo informado")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Retomada aplicada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado"),
                        @ApiResponse(responseCode = "422", description = "Usuário não é membro ativo do círculo")
        })
        @PostMapping("/api/v1/circles/{circleId}/location-sharing/resume")
        ResponseEntity<Void> resumeLocationSharing(
                        @Parameter(description = "ID do círculo") @PathVariable UUID circleId);
}
