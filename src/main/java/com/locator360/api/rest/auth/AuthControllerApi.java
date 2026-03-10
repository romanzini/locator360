package com.locator360.api.rest.auth;

import com.locator360.core.port.in.dto.input.LoginWithEmailInputDto;
import com.locator360.core.port.in.dto.input.LoginWithPhoneInputDto;
import com.locator360.core.port.in.dto.input.ConfirmPasswordResetInputDto;
import com.locator360.core.port.in.dto.input.RequestPasswordResetInputDto;
import com.locator360.core.port.in.dto.input.RefreshTokenInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;
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

        @Operation(summary = "Login com e-mail e senha", description = "Autentica um usuário com e-mail e senha e retorna tokens JWT")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "422", description = "Credenciais inválidas")
        })
        @PostMapping("/login/email")
        ResponseEntity<LoginOutputDto> loginWithEmail(
                        @Valid @RequestBody LoginWithEmailInputDto input);

        @Operation(summary = "Login com telefone", description = "Autentica um usuário com número de telefone e código de verificação")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "422", description = "Credenciais inválidas")
        })
        @PostMapping("/login/phone")
        ResponseEntity<LoginOutputDto> loginWithPhone(
                        @Valid @RequestBody LoginWithPhoneInputDto input);

        @Operation(summary = "Refresh token", description = "Gera um novo par de tokens a partir de um refresh token válido")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "422", description = "Token inválido ou expirado")
        })
        @PostMapping("/refresh")
        ResponseEntity<LoginOutputDto> refreshToken(
                        @Valid @RequestBody RefreshTokenInputDto input);

        @Operation(summary = "Logout", description = "Encerra a sessão do usuário autenticado")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado")
        })
        @PostMapping("/logout")
        ResponseEntity<Void> logout();

        @Operation(summary = "Solicitar reset de senha", description = "Gera um token de recuperação e envia por e-mail ou SMS")
        @ApiResponses({
                        @ApiResponse(responseCode = "202", description = "Solicitação aceita"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos")
        })
        @PostMapping("/password-reset/request")
        ResponseEntity<Void> requestPasswordReset(
                        @Valid @RequestBody RequestPasswordResetInputDto input);

        @Operation(summary = "Confirmar reset de senha", description = "Valida o token de recuperação e define uma nova senha")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "422", description = "Token inválido ou expirado")
        })
        @PostMapping("/password-reset/confirm")
        ResponseEntity<Void> confirmPasswordReset(
                        @Valid @RequestBody ConfirmPasswordResetInputDto input);
}
