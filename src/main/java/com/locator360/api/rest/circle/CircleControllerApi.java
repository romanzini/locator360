package com.locator360.api.rest.circle;

import com.locator360.core.port.in.dto.input.CreateCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleOutputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Circles", description = "Gestão de círculos (grupos)")
public interface CircleControllerApi {

    @Operation(summary = "Criar novo círculo", description = "Cria um novo círculo e adiciona o criador como administrador")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Círculo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    ResponseEntity<CircleOutputDto> create(@Valid @RequestBody CreateCircleInputDto input);
}
