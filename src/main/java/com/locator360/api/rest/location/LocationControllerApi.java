package com.locator360.api.rest.location;

import com.locator360.core.port.in.dto.input.StreamLocationInputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Locations", description = "Rastreamento de localização em tempo real")
public interface LocationControllerApi {

    @Operation(summary = "Enviar lote de eventos de localização",
            description = "Endpoint usado pelo app mobile para enviar eventos de localização em lote")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Eventos aceitos para processamento"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping("/stream")
    ResponseEntity<Void> stream(@Valid @RequestBody StreamLocationInputDto input);
}
