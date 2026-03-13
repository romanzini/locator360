package com.locator360.api.rest.user;

import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.DeviceOutputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
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
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "Gestão de perfil do usuário")
public interface UserControllerApi {

  @Operation(summary = "Obter perfil do usuário atual", description = "Retorna os dados de perfil do usuário autenticado")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Perfil do usuário"),
      @ApiResponse(responseCode = "401", description = "Não autenticado")
  })
  @GetMapping("/me")
  ResponseEntity<UserProfileOutputDto> getProfile();

  @Operation(summary = "Atualizar perfil do usuário atual", description = "Atualiza parcialmente os dados de perfil do usuário autenticado")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Perfil atualizado"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "401", description = "Não autenticado"),
      @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
  })
  @PatchMapping("/me")
  ResponseEntity<UserProfileOutputDto> updateProfile(
      @Valid @RequestBody UpdateUserProfileInputDto input);

  @Operation(summary = "Listar dispositivos do usuário", description = "Retorna todos os dispositivos registrados do usuário autenticado")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de dispositivos"),
      @ApiResponse(responseCode = "401", description = "Não autenticado")
  })
  @GetMapping("/me/devices")
  ResponseEntity<List<DeviceOutputDto>> listDevices();

  @Operation(summary = "Revogar sessão de dispositivo", description = "Desativa um dispositivo específico, revogando sua sessão")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Dispositivo revogado"),
      @ApiResponse(responseCode = "401", description = "Não autenticado"),
      @ApiResponse(responseCode = "422", description = "Dispositivo não encontrado ou não pertence ao usuário")
  })
  @DeleteMapping("/me/devices/{deviceId}")
  ResponseEntity<Void> revokeDevice(
      @Parameter(description = "ID do dispositivo") @PathVariable UUID deviceId);
}
