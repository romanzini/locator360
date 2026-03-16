package com.locator360.api.rest.place;

import com.locator360.core.port.in.dto.input.CreatePlaceInputDto;
import com.locator360.core.port.in.dto.input.UpdatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Places", description = "Gestão de lugares e geofencing")
public interface PlaceControllerApi {

  @Operation(summary = "Listar lugares do círculo", description = "Retorna todos os lugares ativos de um círculo")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de lugares retornada com sucesso"),
      @ApiResponse(responseCode = "401", description = "Não autenticado"),
      @ApiResponse(responseCode = "422", description = "Usuário não é membro do círculo")
  })
  @GetMapping
  ResponseEntity<List<PlaceOutputDto>> list(
      @Parameter(description = "ID do círculo") @PathVariable UUID circleId);

  @Operation(summary = "Criar lugar", description = "Cria um novo lugar no círculo com geofence")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Lugar criado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "401", description = "Não autenticado"),
      @ApiResponse(responseCode = "422", description = "Usuário não é membro do círculo")
  })
  @PostMapping
  ResponseEntity<PlaceOutputDto> create(
      @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
      @Valid @RequestBody CreatePlaceInputDto input);

  @Operation(summary = "Obter detalhes do lugar", description = "Retorna os detalhes de um lugar específico")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Detalhes do lugar retornados com sucesso"),
      @ApiResponse(responseCode = "401", description = "Não autenticado"),
      @ApiResponse(responseCode = "422", description = "Lugar não encontrado ou usuário não é membro")
  })
  @GetMapping("/{placeId}")
  ResponseEntity<PlaceOutputDto> get(
      @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
      @Parameter(description = "ID do lugar") @PathVariable UUID placeId);

  @Operation(summary = "Atualizar lugar", description = "Atualiza os dados de um lugar existente")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lugar atualizado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "401", description = "Não autenticado"),
      @ApiResponse(responseCode = "422", description = "Lugar não encontrado ou usuário não é membro")
  })
  @PatchMapping("/{placeId}")
  ResponseEntity<PlaceOutputDto> update(
      @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
      @Parameter(description = "ID do lugar") @PathVariable UUID placeId,
      @Valid @RequestBody UpdatePlaceInputDto input);

  @Operation(summary = "Excluir lugar", description = "Desativa um lugar (soft delete)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Lugar excluído com sucesso"),
      @ApiResponse(responseCode = "401", description = "Não autenticado"),
      @ApiResponse(responseCode = "422", description = "Lugar não encontrado ou usuário não é membro")
  })
  @DeleteMapping("/{placeId}")
  ResponseEntity<Void> delete(
      @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
      @Parameter(description = "ID do lugar") @PathVariable UUID placeId);
}
