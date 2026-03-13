# Matriz de Requisitos Funcionais — Módulos 4 & 5

> Controle de implementação dos RFs da especificação funcional (módulos 4 e 5).
> Atualizar após cada RF implementado ou completado.

## Legenda

| Status | Significado |
|--------|-------------|
| ✅ Impl | Implementado e testado |
| 🔶 Parcial | Infraestrutura/domínio existe, falta endpoint ou lógica complementar |
| ❌ Pendente | Não implementado |
| ⏭️ Futuro | Depende de módulo/infra externa não disponível ainda |

---

## Módulo 4 — Conta & Autenticação

### 4.1 Cadastro de Usuário

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-001 | Cadastro via e-mail + senha | ✅ Impl | `RegisterUserService.registerWithEmail()`, endpoint `POST /auth/register/email` |
| RF-001 | Cadastro via telefone + SMS | ✅ Impl | `RegisterUserService.registerWithPhone()`, endpoint `POST /auth/register/phone` |
| RF-001 | Login social (Google, Apple, Facebook) | ⏭️ Futuro | `AuthProvider` enum tem GOOGLE/APPLE/FACEBOOK, mas sem fluxo OAuth implementado |
| RF-002 | Validar e-mail/telefone com código de verificação | 🔶 Parcial | `VerificationToken` criado no registro, mas envio real de email/SMS não implementado (falta adapter de infra) |
| RF-003 | Armazenar dados de perfil (nome, foto, sexo, nascimento) | ✅ Impl | `User` entity com fullName, firstName, lastName, profilePhotoUrl, gender, birthDate |
| RF-004 | Redefinição de senha via e-mail/SMS | ✅ Impl | `RequestPasswordResetService` + `ConfirmPasswordResetService`, endpoints `/auth/password-reset/*` |

### 4.2 Login & Sessão

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-005 | Login com múltiplos métodos | ✅ Impl | `LoginService.loginWithEmail()` + `loginWithPhone()`, endpoints `/auth/login/*` |
| RF-006 | Sessão persistente (token) no dispositivo | ✅ Impl | JWT access + refresh tokens via `TokenProvider`, `RefreshTokenService` |
| RF-007 | Logout manual + revogação de sessões em outros dispositivos | ✅ Impl | `LogoutService` (all devices) + `RevokeDeviceService` (device específico), endpoint `DELETE /users/me/devices/{deviceId}` |

### 4.3 Perfil do Usuário

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-008 | Edição de dados de perfil | ✅ Impl | `UpdateUserProfileService`, endpoint `PATCH /users/me` |
| RF-009 | Configuração de idioma, fuso horário, unidades | ✅ Impl | `preferredLanguage`, `timezone`, `distanceUnit` no `UpdateUserProfileInputDto` |
| RF-010 | Exibir status de assinatura (plano) | ⏭️ Futuro | Depende do Módulo 14 (Billing & Plans) — nenhuma entidade de plano implementada |

### 4.4 Identidades, Verificação e Dispositivos

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-011A | Vincular múltiplas identidades (senha, social, telefone) | 🔶 Parcial | `AuthIdentity` + `AuthProvider` suportam multi-provider. **Falta**: endpoint/serviço para vincular identidade adicional a conta existente |
| RF-011B | Tokens de verificação/redefinição c/ expiração e uso único | ✅ Impl | `VerificationToken` com `isExpired()`, `isValid()`, `markUsed()` |
| RF-011C | Gerenciar dispositivos ativos do usuário | ✅ Impl | `ListUserDevicesService`, endpoint `GET /users/me/devices` |
| RF-011D | Revogar sessão em dispositivo específico | ✅ Impl | `RevokeDeviceService`, endpoint `DELETE /users/me/devices/{deviceId}` |

---

## Módulo 5 — Círculos (Grupos)

### 5.1 Criação e Gestão de Círculos

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-011 | Criar múltiplos círculos por usuário | ✅ Impl | `CreateCircleService`, endpoint `POST /circles` |
| RF-012 | Definir atributos (nome, foto/ícone, cor) | ✅ Impl | `Circle` entity com name, photoUrl, colorHex |
| RF-013 | Criador é Administrador por padrão | ✅ Impl | `CreateCircleService` cria `CircleMember.createAdmin()` |

### 5.2 Convite e Entrada em Círculos

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-014 | Gerar código de convite ou link compartilhável | 🔶 Parcial | `CircleInvite.generateInviteCode()` gera código 8-char. **Falta**: geração de link compartilhável (URL) |
| RF-015 | Entrada via código, link ou convite direto | 🔶 Parcial | Entry via código funciona (`JoinCircleService`). **Falta**: entry via link + envio de convite por email/telefone (adapter de infra) |
| RF-016 | Exigir aceite do convite | ✅ Impl | `CircleInvite.accept(userId)` no `JoinCircleService` |
| RF-016A | Notificar membros após entrada (MEMBER_JOINED) | ✅ Impl | `JoinCircleService.publishMemberJoinedNotifications()` publica `NotificationCommand` |
| RF-017 | Aprovar/rejeitar entradas em círculo privado | ❌ Pendente | `PrivacyLevel.INVITE_ONLY` existe, mas sem fila de aprovação |

### 5.3 Papéis e Permissões em Círculos

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-018 | Papéis ADMIN e MEMBER | ✅ Impl | `CircleRole.ADMIN` + `CircleRole.MEMBER`, validações em services |
| RF-019 | Admin: remover membros, transferir admin | ✅ Impl | `RemoveMemberService` + `TransferAdminService`, endpoints `DELETE /circles/{id}/members/{id}` e `PUT .../transfer-admin` |
| RF-019 | Admin: bloquear novo ingresso (círculo fechado) | ❌ Pendente | `CircleSettings` existe mas sem flag de "locked". **Falta**: campo + endpoint |
| RF-019A | Notificar membros após remoção (MEMBER_REMOVED) | ✅ Impl | `RemoveMemberService.publishMemberRemovedNotifications()` |
| RF-019B | Notificar membros após transferência (ADMIN_TRANSFERRED) | ✅ Impl | `TransferAdminService.publishAdminTransferredNotifications()` |

### 5.4 Sair e Excluir Círculo

| RF | Descrição | Status | Evidência / Gap |
|----|-----------|--------|-----------------|
| RF-020 | Sair de um círculo | ✅ Impl | `LeaveCircleService`, endpoint `POST /circles/{id}/leave` |
| RF-020A | Notificar membros restantes (MEMBER_LEFT) | ✅ Impl | `LeaveCircleService.publishMemberLeftNotifications()` |
| RF-021 | Único admin sair: transferir ou notificar | ✅ Impl | `LeaveCircleService` bloqueia saída de admin único com membros (opção "notificar para escolher substituto") |
| RF-022 | Último admin excluir círculo (remoção lógica) | ✅ Impl | `Circle.delete()` + `LeaveCircleService` soft-delete quando último membro sai |

---

## Resumo

| Métrica | Módulo 4 | Módulo 5 | Total |
|---------|----------|----------|-------|
| ✅ Implementado | 12 | 12 | 24 |
| 🔶 Parcial | 3 | 2 | 5 |
| ❌ Pendente | 0 | 2 | 2 |
| ⏭️ Futuro | 2 | 0 | 2 |
| **Total** | **17** | **16** | **33** |

## Próximas Implementações (por prioridade)

| # | RF | Escopo | US Relacionada |
|---|-----|--------|----------------|
| 1 | RF-019 (parcial) | Bloquear novo ingresso em círculo (flag locked em CircleSettings) | US-013 (extensão) |
| 2 | RF-011A | Vincular identidade adicional a conta existente | US-130 |
| 3 | RF-017 | Aprovação/rejeição de entrada em círculo privado | US-012 (extensão) |
| 4 | RF-014 + RF-015 | Link compartilhável de convite + entrada via link | US-011/012 (extensão) |
