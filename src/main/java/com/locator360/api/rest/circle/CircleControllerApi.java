package com.locator360.api.rest.circle;

import com.locator360.core.port.in.dto.input.CreateCircleInputDto;
import com.locator360.core.port.in.dto.input.CreateInviteInputDto;
import com.locator360.core.port.in.dto.input.JoinCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;
import com.locator360.core.port.in.dto.output.CircleOutputDto;
import com.locator360.core.port.in.dto.output.InviteOutputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

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

    @Operation(summary = "Criar convite para círculo", description = "Cria um convite para adicionar membros ao círculo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Convite criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PostMapping("/{circleId}/invites")
    ResponseEntity<InviteOutputDto> createInvite(
            @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
            @Valid @RequestBody CreateInviteInputDto input);

    @Operation(summary = "Entrar em círculo", description = "Entra em um círculo usando um código de convite")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrou no círculo com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "422", description = "Convite inválido, expirado ou limite atingido")
    })
    @PostMapping("/join")
    ResponseEntity<CircleMemberOutputDto> join(@Valid @RequestBody JoinCircleInputDto input);

    @Operation(summary = "Listar membros do círculo", description = "Retorna todos os membros ativos do círculo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de membros retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "422", description = "Usuário não é membro do círculo")
    })
    @GetMapping("/{circleId}/members")
    ResponseEntity<List<CircleMemberOutputDto>> listMembers(
            @Parameter(description = "ID do círculo") @PathVariable UUID circleId);

    @Operation(summary = "Remover membro do círculo", description = "Remove um membro do círculo (apenas administradores)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "422", description = "Solicitante não é admin ou membro não encontrado")
    })
    @DeleteMapping("/{circleId}/members/{memberId}")
    ResponseEntity<Void> removeMember(
            @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
            @Parameter(description = "ID do usuário a ser removido") @PathVariable UUID memberId);

    @Operation(summary = "Transferir administração do círculo", description = "Transfere o papel de administrador para outro membro (apenas administradores)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Administração transferida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "422", description = "Solicitante não é admin ou membro não encontrado")
    })
    @PutMapping("/{circleId}/members/{memberId}/transfer-admin")
    ResponseEntity<Void> transferAdmin(
            @Parameter(description = "ID do círculo") @PathVariable UUID circleId,
            @Parameter(description = "ID do usuário que receberá a administração") @PathVariable UUID memberId);

    @Operation(summary = "Sair do círculo", description = "O usuário autenticado sai do círculo, parando de compartilhar localização")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Saiu do círculo com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "422", description = "Único admin com outros membros — transfira a administração antes")
    })
    @PostMapping("/{circleId}/leave")
    ResponseEntity<Void> leave(
            @Parameter(description = "ID do círculo") @PathVariable UUID circleId);
}
