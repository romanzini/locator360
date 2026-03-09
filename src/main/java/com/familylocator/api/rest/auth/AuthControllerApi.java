package com.familylocator.api.rest.auth;

import com.familylocator.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.familylocator.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.familylocator.core.port.in.dto.output.RegisterUserOutputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "Autenticação e registro de usuários")
public interface AuthControllerApi {

  @Operation(summary = "Registro com e-mail e senha", description = "Cria um novo usuário utilizando e-mail e senha")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "422", description = "Email já cadastrado")
  })
  @PostMapping("/register/email")
  ResponseEntity<RegisterUserOutputDto> registerWithEmail(
      @Valid @RequestBody RegisterWithEmailInputDto input);

  @Operation(summary = "Registro com telefone e código SMS", description = "Cria um novo usuário utilizando telefone e código de verificação SMS")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "422", description = "Telefone já cadastrado")
  })
  @PostMapping("/register/phone")
  ResponseEntity<RegisterUserOutputDto> registerWithPhone(
      @Valid @RequestBody RegisterWithPhoneInputDto input);
}
