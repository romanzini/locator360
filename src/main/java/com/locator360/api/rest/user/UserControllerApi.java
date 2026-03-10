package com.locator360.api.rest.user;

import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
