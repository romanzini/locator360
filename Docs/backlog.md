# Backlog de Produto — Decomposição em Tarefas

> Cada User Story (US) é decomposta em tarefas técnicas seguindo as camadas da **Arquitetura Vexa**:
>
> | Camada | Prefixo | Descrição |
> |--------|---------|-----------|
> | Database | `DB` | Migration Flyway (DDL) |
> | Domain | `DOM` | Entidades, Value Objects, Enums, Domain Services |
> | Port OUT | `POUT` | Interfaces de repositório / serviços externos |
> | Port IN | `PIN` | Interfaces de Use Case + DTOs (Input/Output) |
> | Application | `APP` | Application Services (implementam Port IN) |
> | Infrastructure | `INF` | JPA Entities, Repositories, Kafka Publishers/Consumers |
> | API | `API` | Controllers REST (interface + implementação) |
> | Testes | `TEST` | Testes unitários (Domain + Application) |

---

## Épico 1 – Gestão de Conta e Acesso

### US-001 – Cadastro de conta

- Como **novo usuário**, quero **me cadastrar usando e-mail, telefone ou conta social**, para **começar a usar o app rapidamente**.

> Nesta US, o cadastro deve capturar apenas os dados mínimos para criação da conta.
> Campos de enriquecimento de perfil como `birth_date`, `gender`, `profile_photo_url`, `preferred_language`, `timezone` e `distance_unit`
> permanecem na entidade `users`, mas são preenchidos com defaults ou posteriormente na **US-004 – Atualizar perfil**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `users` | Tabela `users` com campos: id, email, phone_number, full_name, first_name, last_name, birth_date, gender, profile_photo_url, preferred_language, timezone, distance_unit, status, created_at, updated_at |
| 2 | DB | Criar migration `auth_identities` | Tabela `auth_identities` com campos: id, user_id (FK), provider (ENUM), provider_user_id, email, phone_number, password_hash, is_verified, last_login_at, created_at, updated_at |
| 3 | DB | Criar migration `verification_tokens` | Tabela `verification_tokens` com campos: id, user_id (FK), type (ENUM), token, expires_at, used_at, created_at |
| 4 | DOM | Criar entidade `User` | Factory methods `create()` e `restore()`, validações de negócio (email/telefone obrigatório) |
| 5 | DOM | Criar entidade `AuthIdentity` | Factory methods, enum `AuthProvider` (PASSWORD, GOOGLE, APPLE, FACEBOOK, PHONE_SMS) |
| 6 | DOM | Criar entidade `VerificationToken` | Factory methods, enum `TokenType`, validação de expiração |
| 7 | DOM | Criar enum `UserStatus` | Valores: ACTIVE, BLOCKED, PENDING_VERIFICATION |
| 8 | DOM | Criar VOs compartilhados | `Email`, `PhoneNumber`, `DistanceUnit` |
| 9 | POUT | Criar interface `UserRepository` | Métodos: save, findById, findByEmail, findByPhone, existsByEmail |
| 10 | POUT | Criar interface `AuthIdentityRepository` | Métodos: save, findByUserIdAndProvider, findByProviderAndProviderUserId |
| 11 | POUT | Criar interface `VerificationTokenRepository` | Métodos: save, findByToken, findByUserIdAndType |
| 12 | PIN | Criar interface `RegisterUserUseCase` | Métodos: `registerWithEmail(RegisterWithEmailInputDto)` e `registerWithPhone(RegisterWithPhoneInputDto)` |
| 13 | PIN | Criar DTOs de registro mínimo | `RegisterWithEmailInputDto` com campos `email`, `password`, `fullName`; `RegisterWithPhoneInputDto` com `phoneNumber`, `verificationCode`, `fullName` |
| 14 | PIN | Criar `RegisterUserOutputDto` | Campos: id, email, phoneNumber, fullName, firstName, lastName, preferredLanguage, timezone, distanceUnit, status |
| 15 | APP | Implementar `RegisterUserService` | Orquestra: validar duplicidade, criar User (factory) com defaults de perfil, criar AuthIdentity, gerar VerificationToken, salvar via ports |
| 16 | INF | Criar `UserJpaEntity` | Mapeamento JPA da tabela `users` |
| 17 | INF | Criar `AuthIdentityJpaEntity` | Mapeamento JPA da tabela `auth_identities` |
| 18 | INF | Criar `VerificationTokenJpaEntity` | Mapeamento JPA da tabela `verification_tokens` |
| 19 | INF | Implementar `UserJpaRepository` | Implementa `UserRepository` com Spring Data |
| 20 | INF | Implementar `AuthIdentityJpaRepository` | Implementa `AuthIdentityRepository` com Spring Data |
| 21 | INF | Implementar `VerificationTokenJpaRepository` | Implementa `VerificationTokenRepository` com Spring Data |
| 22 | API | Criar interface `AuthControllerApi` | Anotações OpenAPI para `POST /api/v1/auth/register/email` e `POST /api/v1/auth/register/phone` |
| 23 | API | Implementar `AuthController` | Delega para `RegisterUserUseCase`, retorna 201 Created nos fluxos de e-mail e telefone |
| 24 | TEST | Testes unitários `User` (Domain) | Testar factory methods, validações de negócio |
| 25 | TEST | Testes unitários `RegisterUserService` | Testar fluxo completo com mocks dos ports |

---

### US-002 – Login e manutenção de sessão

- Como **usuário cadastrado**, quero **fazer login e manter minha sessão ativa**, para **não precisar digitar meus dados toda vez**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `devices` | Tabela `devices` com campos: id, user_id (FK), platform (ENUM), device_model, os_version, app_version, push_token, is_active, last_seen_at, created_at, updated_at |
| 2 | DOM | Criar entidade `Device` | Factory methods, enum `Platform` (ANDROID, IOS, WEB) |
| 3 | DOM | Criar domain service `AuthenticationService` | Lógica: validar credenciais por provider, verificar status da conta |
| 4 | POUT | Criar interface `DeviceRepository` | Métodos: save, findByUserIdAndPlatform, findByUserId |
| 5 | POUT | Criar interface `TokenProvider` | Métodos: generateAccessToken, generateRefreshToken, validateToken, revokeToken |
| 6 | PIN | Criar interface `LoginUseCase` | Método: execute(LoginInputDto): LoginOutputDto |
| 7 | PIN | Criar interface `RefreshTokenUseCase` | Método: execute(RefreshTokenInputDto): LoginOutputDto |
| 8 | PIN | Criar interface `LogoutUseCase` | Método: execute(LogoutInputDto): void |
| 9 | PIN | Criar `LoginInputDto` | Campos: email, password, provider, providerToken, deviceInfo |
| 10 | PIN | Criar `LoginOutputDto` | Campos: accessToken, refreshToken, expiresIn, userId |
| 11 | APP | Implementar `LoginService` | Orquestra: buscar identidade, validar senha/token social, gerar JWT, registrar dispositivo |
| 12 | APP | Implementar `RefreshTokenService` | Validar refresh token, gerar novo par de tokens |
| 13 | APP | Implementar `LogoutService` | Revogar tokens, desativar dispositivo |
| 14 | INF | Criar `DeviceJpaEntity` | Mapeamento JPA da tabela `devices` |
| 15 | INF | Implementar `DeviceJpaRepository` | Implementa `DeviceRepository` com Spring Data |
| 16 | INF | Implementar `JwtTokenProvider` | Implementa `TokenProvider` com jjwt/nimbus-jose |
| 17 | API | Adicionar endpoints ao `AuthController` | `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` |
| 18 | INF | Configurar Spring Security + JWT Filter | Filtro de autenticação JWT, SecurityConfig |
| 19 | TEST | Testes unitários `AuthenticationService` | Testar validação de credenciais por provider |
| 20 | TEST | Testes unitários `LoginService` | Testar fluxo de login com mocks |

---

### US-003 – Recuperação de senha

- Como **usuário que esqueceu a senha**, quero **recuperar o acesso via e-mail ou SMS**, para **continuar usando o app sem perder minha conta**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | POUT | Criar interface `NotificationSender` | Métodos: sendEmail(to, subject, body), sendSms(to, message) |
| 2 | PIN | Criar interface `RequestPasswordResetUseCase` | Método: execute(RequestPasswordResetInputDto): void |
| 3 | PIN | Criar interface `ConfirmPasswordResetUseCase` | Método: execute(ConfirmPasswordResetInputDto): void |
| 4 | PIN | Criar `RequestPasswordResetInputDto` | Campos: email ou phoneNumber |
| 5 | PIN | Criar `ConfirmPasswordResetInputDto` | Campos: token, newPassword |
| 6 | APP | Implementar `RequestPasswordResetService` | Gerar token de reset, enviar via email/SMS |
| 7 | APP | Implementar `ConfirmPasswordResetService` | Validar token, atualizar password_hash na AuthIdentity |
| 8 | INF | Implementar `EmailNotificationSender` | Adapter para envio de e-mail (SMTP / provider externo) |
| 9 | API | Adicionar endpoints ao `AuthController` | `POST /api/v1/auth/password-reset/request`, `POST /api/v1/auth/password-reset/confirm` |
| 10 | TEST | Testes unitários `RequestPasswordResetService` | Testar geração de token e envio |
| 11 | TEST | Testes unitários `ConfirmPasswordResetService` | Testar validação de token expirado/inválido |

---

### US-004 – Atualizar perfil

- Como **usuário**, quero **editar meu perfil (nome, foto, idioma, fuso, unidade)**, para **personalizar minha experiência**.

> Esta US absorve os dados de enriquecimento de perfil que não entram no cadastro mínimo da **US-001**,
> incluindo `birth_date`, `gender`, `profile_photo_url`, `preferred_language`, `timezone` e `distance_unit`.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `UpdateUserProfileUseCase` | Método: execute(UUID userId, UpdateUserProfileInputDto): UserProfileOutputDto |
| 2 | PIN | Criar interface `GetUserProfileUseCase` | Método: execute(UUID userId): UserProfileOutputDto |
| 3 | PIN | Criar `UpdateUserProfileInputDto` | Campos: fullName, firstName, lastName, birthDate, gender, profilePhotoUrl, preferredLanguage, timezone, distanceUnit |
| 4 | PIN | Criar `UserProfileOutputDto` | Campos: id, email, phoneNumber, fullName, firstName, lastName, birthDate, gender, profilePhotoUrl, preferredLanguage, timezone, distanceUnit, status, planCode |
| 5 | APP | Implementar `UpdateUserProfileService` | Buscar User, aplicar alterações parciais de perfil, preservar campos não enviados e salvar |
| 6 | APP | Implementar `GetUserProfileService` | Buscar User + Subscription ativa, montar output |
| 7 | API | Criar interface `UserControllerApi` | Anotações OpenAPI para `GET /api/v1/users/me` e `PATCH /api/v1/users/me` |
| 8 | API | Implementar `UserController` | Delega para use cases, retorna 200 OK |
| 9 | TEST | Testes unitários `UpdateUserProfileService` | Testar atualização parcial e validações |

---

## Épico 2 – Círculos (Grupos Familiares)

### US-010 – Criar círculo

- Como **usuário**, quero **criar um círculo (ex.: "Família")**, para **agrupar as pessoas que desejo monitorar**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `circles` | Tabela `circles` com campos: id, name, description, photo_url, color_hex, privacy_level (ENUM), created_by_user_id (FK), created_at, updated_at |
| 2 | DB | Criar migration `circle_members` | Tabela `circle_members` com campos: id, circle_id (FK), user_id (FK), role (ENUM), status (ENUM), joined_at, left_at, created_at, updated_at. Constraint UNIQUE(circle_id, user_id) |
| 3 | DB | Criar migration `circle_settings` | Tabela `circle_settings` com campos: id, circle_id (FK unique), driving_alert_level (ENUM), allow_member_chat, allow_member_sos, created_at, updated_at |
| 4 | DOM | Criar entidade `Circle` | Factory methods `create()` e `restore()`, validações de negócio (nome obrigatório) |
| 5 | DOM | Criar entidade `CircleMember` | Factory methods, enum `CircleRole` (ADMIN, MEMBER), enum `MemberStatus` (ACTIVE, PENDING, REMOVED) |
| 6 | DOM | Criar entidade `CircleSettings` | Factory methods com valores default |
| 7 | DOM | Criar enum `PrivacyLevel` | Valores: OPEN_WITH_CODE, INVITE_ONLY |
| 8 | POUT | Criar interface `CircleRepository` | Métodos: save, findById, findByCreatedByUserId |
| 9 | POUT | Criar interface `CircleMemberRepository` | Métodos: save, findByCircleId, findByUserId, findByCircleIdAndUserId, countByCircleId |
| 10 | POUT | Criar interface `CircleSettingsRepository` | Métodos: save, findByCircleId |
| 11 | PIN | Criar interface `CreateCircleUseCase` | Método: execute(UUID userId, CreateCircleInputDto): CircleOutputDto |
| 12 | PIN | Criar `CreateCircleInputDto` | Campos: name, description, photoUrl, colorHex, privacyLevel |
| 13 | PIN | Criar `CircleOutputDto` | Campos: id, name, description, photoUrl, colorHex, privacyLevel, createdByUserId, createdAt |
| 14 | APP | Implementar `CreateCircleService` | Criar Circle (factory), adicionar criador como ADMIN em CircleMember, criar CircleSettings default |
| 15 | INF | Criar `CircleJpaEntity` | Mapeamento JPA da tabela `circles` |
| 16 | INF | Criar `CircleMemberJpaEntity` | Mapeamento JPA da tabela `circle_members` |
| 17 | INF | Criar `CircleSettingsJpaEntity` | Mapeamento JPA da tabela `circle_settings` |
| 18 | INF | Implementar `CircleJpaRepository` | Implementa `CircleRepository` com Spring Data |
| 19 | INF | Implementar `CircleMemberJpaRepository` | Implementa `CircleMemberRepository` com Spring Data |
| 20 | INF | Implementar `CircleSettingsJpaRepository` | Implementa `CircleSettingsRepository` com Spring Data |
| 21 | API | Criar interface `CircleControllerApi` | Anotações OpenAPI para `POST /api/v1/circles` |
| 22 | API | Implementar `CircleController` | Delega para `CreateCircleUseCase`, retorna 201 Created |
| 23 | TEST | Testes unitários `Circle` (Domain) | Testar factory methods e validações |
| 24 | TEST | Testes unitários `CreateCircleService` | Testar criação com auto-adição de admin |

---

### US-011 – Convidar pessoas para o círculo

- Como **administrador de círculo**, quero **convidar pessoas via código ou link**, para **adicionar membros facilmente**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `circle_invites` | Tabela `circle_invites` com campos: id, circle_id (FK), invited_by_user_id (FK), target_email, target_phone, invite_code (unique), status (ENUM), accepted_by_user_id (FK nullable), expires_at, created_at, updated_at |
| 2 | DOM | Criar entidade `CircleInvite` | Factory methods, enum `InviteStatus` (PENDING, ACCEPTED, EXPIRED, CANCELLED), geração de invite_code |
| 3 | DOM | Criar domain service `CircleMembershipService` | Lógica: validar limite de membros por plano (Free=5, Premium=10) |
| 4 | POUT | Criar interface `CircleInviteRepository` | Métodos: save, findByInviteCode, findByCircleId, findByCircleIdAndStatus |
| 5 | POUT | Criar interface `SubscriptionRepository` | Métodos: findActiveByUserId (necessário para checar plano) |
| 6 | PIN | Criar interface `CreateInviteUseCase` | Método: execute(UUID userId, UUID circleId, CreateInviteInputDto): InviteOutputDto |
| 7 | PIN | Criar `CreateInviteInputDto` | Campos: targetEmail, targetPhone |
| 8 | PIN | Criar `InviteOutputDto` | Campos: id, circleId, inviteCode, status, expiresAt |
| 9 | APP | Implementar `CreateInviteService` | Validar que userId é ADMIN do circle, verificar limite de membros pelo plano, gerar código, salvar convite |
| 10 | API | Adicionar endpoint ao `CircleController` | `POST /api/v1/circles/{circleId}/invites` |
| 11 | TEST | Testes unitários `CircleMembershipService` | Testar validação de limite por plano |
| 12 | TEST | Testes unitários `CreateInviteService` | Testar geração de convite com validações |

---

### US-012 – Entrar em círculo com código/link

- Como **usuário convidado**, quero **usar um código ou link para entrar num círculo**, para **compartilhar minha localização com esse grupo**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `JoinCircleUseCase` | Método: execute(UUID userId, JoinCircleInputDto): CircleMemberOutputDto |
| 2 | PIN | Criar `JoinCircleInputDto` | Campos: inviteCode |
| 3 | PIN | Criar `CircleMemberOutputDto` | Campos: id, circleId, userId, role, status, joinedAt |
| 4 | POUT | Criar interface `NotificationCommandPublisher` | Método: publish(NotificationCommand): void — publica no Kafka `notification.commands`; `NotificationCommand` com campos: type (NotificationType), recipientUserId (UUID), circleId (UUID nullable), payload (Map\<String, Object\>) |
| 5 | POUT | Criar interface `CircleMemberReadRepository` | Método adicional: findActiveByCircleId(UUID circleId): List\<CircleMember\> — necessário para buscar destinatários da notificação |
| 6 | APP | Implementar `JoinCircleService` | Validar código (existência, expiração, status), verificar limite do plano, criar CircleMember, atualizar invite para ACCEPTED, publicar `MEMBER_JOINED` para todos os outros membros ativos do círculo via `NotificationCommandPublisher` |
| 7 | INF | Implementar `KafkaNotificationCommandPublisher` | Implementa `NotificationCommandPublisher` publicando no tópico `notification.commands` particionado por circleId |
| 8 | API | Adicionar endpoint ao `CircleController` | `POST /api/v1/circles/join` |
| 9 | TEST | Testes unitários `JoinCircleService` | Testar código inválido, expirado, limite atingido, e verificar publicação de `MEMBER_JOINED` para todos os membros existentes do círculo |

---

### US-013 – Gerenciar membros do círculo

- Como **administrador de círculo**, quero **remover membros e transferir administração**, para **manter o grupo organizado e seguro**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `RemoveMemberUseCase` | Método: execute(UUID adminId, UUID circleId, UUID memberId): void |
| 2 | PIN | Criar interface `TransferAdminUseCase` | Método: execute(UUID adminId, UUID circleId, UUID newAdminId): void |
| 3 | PIN | Criar interface `ListCircleMembersUseCase` | Método: execute(UUID userId, UUID circleId): List\<CircleMemberOutputDto\> |
| 4 | APP | Implementar `RemoveMemberService` | Validar que solicitante é ADMIN, marcar membro como REMOVED, atualizar left_at, publicar `MEMBER_REMOVED` para todos os demais membros ativos do círculo via `NotificationCommandPublisher` (payload: memberId removido, nome, circleId) |
| 5 | APP | Implementar `TransferAdminService` | Validar que solicitante é ADMIN, alterar roles (antigo ADMIN → MEMBER, novo ADMIN), publicar `ADMIN_TRANSFERRED` para todos os membros do círculo via `NotificationCommandPublisher` (payload: novo adminId, nome, circleId) |
| 6 | APP | Implementar `ListCircleMembersService` | Buscar membros ativos do círculo |
| 7 | API | Adicionar endpoints ao `CircleController` | `DELETE /api/v1/circles/{circleId}/members/{memberId}`, `PUT /api/v1/circles/{circleId}/members/{memberId}/transfer-admin`, `GET /api/v1/circles/{circleId}/members` |
| 8 | TEST | Testes unitários `RemoveMemberService` | Testar remoção por admin, tentativa por membro comum, e verificar publicação de `MEMBER_REMOVED` para os demais membros |
| 9 | TEST | Testes unitários `TransferAdminService` | Testar transferência, validações de role, e verificar publicação de `ADMIN_TRANSFERRED` para todos os membros |

---

### US-014 – Sair de um círculo

- Como **membro de um círculo**, quero **sair do círculo quando quiser**, para **parar de compartilhar minha localização com aquele grupo**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `LeaveCircleUseCase` | Método: execute(UUID userId, UUID circleId): void |
| 2 | APP | Implementar `LeaveCircleService` | Verificar se é único admin (impedir saída ou excluir círculo), marcar como REMOVED, parar compartilhamento de localização, publicar `MEMBER_LEFT` para todos os membros restantes do círculo via `NotificationCommandPublisher` (payload: userId que saiu, nome, circleId) |
| 3 | API | Adicionar endpoint ao `CircleController` | `POST /api/v1/circles/{circleId}/leave` |
| 4 | TEST | Testes unitários `LeaveCircleService` | Testar saída normal, cenário de último admin, e verificar publicação de `MEMBER_LEFT` para os membros restantes |

---

## Épico 3 – Localização em Tempo Real

### US-020 – Compartilhar minha localização com o círculo

- Como **membro de um círculo**, quero **compartilhar minha localização em tempo real**, para **que minha família saiba onde estou**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `locations` | Tabela `locations` com campos: id, user_id (FK), circle_id (FK nullable), latitude, longitude, accuracy_meters, speed_mps, heading_degrees, altitude_meters, source (ENUM), recorded_at, received_at, is_moving, battery_level, created_at. Índice geoespacial PostGIS |
| 2 | DB | Criar migration `location_sharing_states` | Tabela `location_sharing_states` com campos: id, user_id (FK), circle_id (FK), is_sharing_location, is_history_enabled, paused_until, last_known_location_id (FK nullable), last_updated_at. Constraint UNIQUE(user_id, circle_id) |
| 3 | DOM | Criar entidade `Location` | Factory methods, enum `LocationSource` (GPS, NETWORK, FUSED) |
| 4 | DOM | Criar entidade `LocationSharingState` | Factory methods, lógica de pausa/retomada |
| 5 | POUT | Criar interface `LocationRepository` | Métodos: saveAll(List), findByUserIdAndRecordedAtBetween |
| 6 | POUT | Criar interface `LocationSharingStateRepository` | Métodos: save, findByUserIdAndCircleId |
| 7 | POUT | Criar interface `LocationEventPublisher` | Método: publish(List\<LocationEvent\>) — publica no Kafka `location.events` |
| 8 | POUT | Criar interface `LastLocationCache` | Métodos: save(userId, Location), findByUserId(userId) — abstração do Redis |
| 9 | PIN | Criar interface `StreamLocationUseCase` | Método: execute(UUID userId, StreamLocationInputDto): void |
| 10 | PIN | Criar `StreamLocationInputDto` | Campos: List\<LocationPointDto\> (lat, lon, accuracy, speed, heading, altitude, source, recordedAt, batteryLevel, activityType) |
| 11 | APP | Implementar `StreamLocationService` | Converter DTOs para domain, publicar no Kafka via port, atualizar última localização no Redis, retornar 202 |
| 12 | INF | Criar `LocationJpaEntity` | Mapeamento JPA com PostGIS (`@Column(columnDefinition = "geometry(Point,4326)")`) |
| 13 | INF | Criar `LocationSharingStateJpaEntity` | Mapeamento JPA |
| 14 | INF | Implementar `LocationJpaRepository` | Implementa `LocationRepository` com Spring Data |
| 15 | INF | Implementar `LocationSharingStateJpaRepository` | Implementa `LocationSharingStateRepository` |
| 16 | INF | Implementar `KafkaLocationEventPublisher` | Publica eventos no tópico `location.events` particionado por userId |
| 17 | INF | Implementar `RedisLastLocationCache` | Implementa `LastLocationCache` usando RedisTemplate |
| 18 | INF | Configurar tópico Kafka `location.events` | Configuração de tópico, partições, retenção |
| 19 | API | Criar interface `LocationControllerApi` | Anotações OpenAPI para `POST /api/v1/locations/stream` |
| 20 | API | Implementar `LocationController` | Delega para `StreamLocationUseCase`, retorna 202 Accepted |
| 21 | TEST | Testes unitários `Location` (Domain) | Testar factory methods e validações |
| 22 | TEST | Testes unitários `StreamLocationService` | Testar publicação e cache com mocks |

---

### US-021 – Ver membros no mapa

- Como **membro de um círculo**, quero **ver todos os membros em um mapa**, para **saber rapidamente onde cada um está**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `GetCircleMembersLocationUseCase` | Método: execute(UUID userId, UUID circleId): List\<MemberLocationOutputDto\> |
| 2 | PIN | Criar `MemberLocationOutputDto` | Campos: userId, fullName, profilePhotoUrl, latitude, longitude, accuracy, speed, isMoving, batteryLevel, lastUpdatedAt |
| 3 | APP | Implementar `GetCircleMembersLocationService` | Buscar membros do círculo, para cada um buscar última localização do Redis, verificar estado de compartilhamento |
| 4 | API | Adicionar endpoint ao `LocationController` | `GET /api/v1/circles/{circleId}/members/locations` |
| 5 | TEST | Testes unitários `GetCircleMembersLocationService` | Testar cenários com membros pausados, sem localização |

---

### US-022 – Pausar meu compartilhamento de localização

- Como **usuário preocupado com privacidade**, quero **pausar temporariamente o envio de minha localização**, para **ter momentos de privacidade**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `PauseLocationSharingUseCase` | Método: execute(UUID userId, UUID circleId, PauseLocationInputDto): void |
| 2 | PIN | Criar interface `ResumeLocationSharingUseCase` | Método: execute(UUID userId, UUID circleId): void |
| 3 | PIN | Criar `PauseLocationInputDto` | Campos: pausedUntil (opcional, pausa indefinida se null) |
| 4 | APP | Implementar `PauseLocationSharingService` | Atualizar LocationSharingState para is_sharing_location=false, definir pausedUntil |
| 5 | APP | Implementar `ResumeLocationSharingService` | Atualizar LocationSharingState para is_sharing_location=true, limpar pausedUntil |
| 6 | API | Adicionar endpoints ao `LocationController` | `POST /api/v1/circles/{circleId}/location-sharing/pause`, `POST /api/v1/circles/{circleId}/location-sharing/resume` |
| 7 | TEST | Testes unitários dos services | Testar pausa com e sem tempo definido, retomada |

---

## Épico 4 – Lugares (Geofences) e Alertas de Entrada/Saída

### US-030 – Cadastrar um lugar (casa, escola, trabalho)

- Como **membro de um círculo**, quero **cadastrar lugares importantes em um mapa**, para **receber alertas quando alguém chegar ou sair**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `places` | Tabela `places` com campos: id, circle_id (FK), name, type (ENUM), address_text, latitude, longitude, radius_meters, is_active, created_by_user_id (FK), created_at, updated_at |
| 2 | DB | Criar migration `place_alert_policies` | Tabela `place_alert_policies` com campos de configuração de alertas |
| 3 | DB | Criar migration `place_alert_targets` | Tabela `place_alert_targets` (lista custom de destinatários) |
| 4 | DOM | Criar entidade `Place` | Factory methods, enum `PlaceType` (HOME, SCHOOL, WORK, OTHER), validação de raio |
| 5 | DOM | Criar entidade `PlaceAlertPolicy` | Factory methods, enum `TargetType` (ALL_MEMBERS, ADMINS_ONLY, CUSTOM_LIST) |
| 6 | DOM | Criar entidade `PlaceAlertTarget` | Associação policy → user |
| 7 | POUT | Criar interface `PlaceRepository` | Métodos: save, findById, findByCircleId, deleteById |
| 8 | POUT | Criar interface `PlaceAlertPolicyRepository` | Métodos: save, findByPlaceId |
| 9 | PIN | Criar interface `CreatePlaceUseCase` | Método: execute(UUID userId, UUID circleId, CreatePlaceInputDto): PlaceOutputDto |
| 10 | PIN | Criar interface `UpdatePlaceUseCase` | Método: execute(UUID userId, UUID placeId, UpdatePlaceInputDto): PlaceOutputDto |
| 11 | PIN | Criar interface `DeletePlaceUseCase` | Método: execute(UUID userId, UUID placeId): void |
| 12 | PIN | Criar interface `ListPlacesUseCase` | Método: execute(UUID userId, UUID circleId): List\<PlaceOutputDto\> |
| 13 | PIN | Criar DTOs | `CreatePlaceInputDto`, `UpdatePlaceInputDto`, `PlaceOutputDto` |
| 14 | APP | Implementar `CreatePlaceService` | Validar pertencimento ao círculo, criar Place (factory), salvar |
| 15 | APP | Implementar `UpdatePlaceService` | Buscar, validar permissão, atualizar |
| 16 | APP | Implementar `DeletePlaceService` | Buscar, validar permissão, desativar (soft-delete) |
| 17 | APP | Implementar `ListPlacesService` | Listar places ativos do círculo |
| 18 | INF | Criar `PlaceJpaEntity` | Mapeamento JPA com PostGIS |
| 19 | INF | Criar `PlaceAlertPolicyJpaEntity` | Mapeamento JPA |
| 20 | INF | Criar `PlaceAlertTargetJpaEntity` | Mapeamento JPA |
| 21 | INF | Implementar repositórios JPA | `PlaceJpaRepository`, `PlaceAlertPolicyJpaRepository` |
| 22 | API | Criar interface `PlaceControllerApi` | Anotações OpenAPI para CRUD `/api/v1/circles/{circleId}/places` |
| 23 | API | Implementar `PlaceController` | Delega para use cases de Place |
| 24 | TEST | Testes unitários dos services | Testar CRUD completo com validações |

---

### US-031 – Receber alertas de entrada/saída de lugar

- Como **responsável por um filho**, quero **ser avisado quando ele chegar ou sair da escola ou de casa**, para **saber se está seguro**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `place_events` | Tabela `place_events` com campos: id, place_id (FK), circle_id (FK), user_id (FK), event_type (ENUM ENTER/EXIT), location_id (FK), occurred_at, created_at |
| 2 | DOM | Criar entidade `PlaceEvent` | Factory methods, enum `PlaceEventType` (ENTER, EXIT) |
| 3 | DOM | Criar domain service `GeofenceDetectionService` | Lógica: verificar se ponto está dentro do raio usando cálculo de distância |
| 4 | POUT | Criar interface `PlaceEventRepository` | Métodos: save, findByPlaceIdAndUserId (último evento para determinar transição) |
| 5 | POUT | Criar interface `GeofenceEventPublisher` | Método: publish(GeofenceEvent) — publica no Kafka `geofence.events` |
| 6 | POUT | Criar interface `GeofenceQueryPort` | Método: findPlacesContainingPoint(lat, lon, circleIds): List\<Place\> — usa PostGIS ST_DWithin |
| 7 | INF | Criar `PlaceEventJpaEntity` | Mapeamento JPA |
| 8 | INF | Implementar `PlaceEventJpaRepository` | Implementa `PlaceEventRepository` |
| 9 | INF | Implementar `PostgisGeofenceQuery` | Implementa `GeofenceQueryPort` com query nativa PostGIS |
| 10 | INF | Implementar `KafkaGeofenceEventPublisher` | Publica eventos no tópico `geofence.events` |
| 11 | INF | Configurar tópico Kafka `geofence.events` | Configuração de tópico particionado por circleId |
| 12 | APP | Implementar `GeofenceProcessingService` | Consome location event, busca geofences ao redor, detecta transições (enter/exit), publica geofence.events |
| 13 | API (Kafka) | Criar `GeofenceConsumer` | Consumer Kafka que escuta `location.events` e delega para `GeofenceProcessingService` |
| 14 | TEST | Testes unitários `GeofenceDetectionService` | Testar detecção de entrada/saída com coordenadas simuladas |
| 15 | TEST | Testes unitários `GeofenceProcessingService` | Testar fluxo completo com mocks |

---

### US-032 – Configurar horários de relevância de alerta

- Como **usuário**, quero **definir horários em que os alertas fazem sentido**, para **não ser incomodado fora do horário útil**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `UpdatePlaceAlertPolicyUseCase` | Método: execute(UUID userId, UUID placeId, UpdatePlaceAlertPolicyInputDto): PlaceAlertPolicyOutputDto |
| 2 | PIN | Criar `UpdatePlaceAlertPolicyInputDto` | Campos: alertOnEnter, alertOnExit, daysOfWeek, startTime, endTime, targetType, targetUserIds |
| 3 | PIN | Criar `PlaceAlertPolicyOutputDto` | Todos os campos da política |
| 4 | APP | Implementar `UpdatePlaceAlertPolicyService` | Validar permissão, atualizar política, gerenciar lista custom de targets |
| 5 | API | Adicionar endpoint ao `PlaceController` | `PUT /api/v1/circles/{circleId}/places/{placeId}/alert-policy` |
| 6 | TEST | Testes unitários | Testar configuração de janela horária e destinatários |

---

## Épico 5 – Histórico de Localização

### US-040 – Ver histórico de um dia

- Como **membro de um círculo**, quero **ver o trajeto de um dia para um membro**, para **entender por onde ele passou**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `GetLocationHistoryUseCase` | Método: execute(UUID requesterId, UUID targetUserId, UUID circleId, GetLocationHistoryInputDto): LocationHistoryOutputDto |
| 2 | PIN | Criar `GetLocationHistoryInputDto` | Campos: startDate, endDate |
| 3 | PIN | Criar `LocationHistoryOutputDto` | Campos: userId, points (List\<LocationPointOutputDto\>), totalPoints |
| 4 | PIN | Criar `LocationPointOutputDto` | Campos: latitude, longitude, accuracy, speed, isMoving, recordedAt, addressText |
| 5 | APP | Implementar `GetLocationHistoryService` | Validar que requester pertence ao mesmo círculo, verificar estado de compartilhamento, buscar pontos do período |
| 6 | API | Adicionar endpoint ao `LocationController` | `GET /api/v1/circles/{circleId}/members/{memberId}/location-history?startDate=&endDate=` |
| 7 | TEST | Testes unitários `GetLocationHistoryService` | Testar acesso permitido/negado, período válido |

---

### US-041 – Restringir histórico por tipo de plano

- Como **gestor de produto**, quero **limitar o período de histórico por plano**, para **oferecer diferenciação entre plano gratuito e premium**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DOM | Criar domain service `HistoryRetentionService` | Lógica: validar período permitido com base no plano (30 dias para todos nesta versão) |
| 2 | APP | Atualizar `GetLocationHistoryService` | Injetar `HistoryRetentionService`, limitar consulta ao período máximo do plano |
| 3 | INF | Criar job agendado `LocationHistoryCleanupJob` | Spring `@Scheduled` para soft-delete de dados com mais de 30 dias |
| 4 | TEST | Testes unitários `HistoryRetentionService` | Testar limites por plano |

---

## Épico 6 – Direção e Segurança no Trânsito

### US-050 – Registrar viagens de carro automaticamente

- Como **usuário que dirige**, quero **que o app detecte automaticamente minhas viagens**, para **não precisar iniciar/parar manualmente**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `drives` | Tabela `drives` com campos: id, user_id (FK), circle_id (FK nullable), mode (ENUM CAR), start_location_id (FK), end_location_id (FK), start_time, end_time, distance_meters, duration_seconds, max_speed_mps, avg_speed_mps, safety_score, created_at, updated_at |
| 2 | DB | Criar migration `drive_events` | Tabela `drive_events` com campos: id, drive_id (FK), user_id (FK), event_type (ENUM), severity (ENUM), location_id (FK), speed_mps, occurred_at, created_at |
| 3 | DOM | Criar entidade `Drive` | Factory methods, enum `DriveMode` (CAR), métodos para calcular resumo |
| 4 | DOM | Criar entidade `DriveEvent` | Factory methods, enum `DriveEventType` (SPEEDING, HARD_BRAKE, HARD_ACCEL, HARD_TURN, PHONE_USAGE), enum `Severity` (LOW, MEDIUM, HIGH) |
| 5 | DOM | Criar domain service `DriveDetectionService` | Algoritmo stateful: detectar início (velocidade > limiar), fim (parado por X minutos), calcular resumo |
| 6 | POUT | Criar interface `DriveRepository` | Métodos: save, findById, findByUserIdAndStartTimeBetween |
| 7 | POUT | Criar interface `DriveEventRepository` | Métodos: save, saveAll, findByDriveId |
| 8 | POUT | Criar interface `DriveEventPublisher` | Método: publish(DriveEventMessage) — publica no Kafka `drive.events` |
| 9 | INF | Criar `DriveJpaEntity` | Mapeamento JPA |
| 10 | INF | Criar `DriveEventJpaEntity` | Mapeamento JPA |
| 11 | INF | Implementar `DriveJpaRepository` | Implementa `DriveRepository` |
| 12 | INF | Implementar `DriveEventJpaRepository` | Implementa `DriveEventRepository` |
| 13 | INF | Implementar `KafkaDriveEventPublisher` | Publica eventos no tópico `drive.events` |
| 14 | INF | Configurar tópico Kafka `drive.events` | Configuração de tópico particionado por userId |
| 15 | APP | Implementar `DriveDetectionProcessingService` | Consome location events, mantém estado da viagem em andamento, detecta início/fim, persiste Drive e publica evento |
| 16 | API (Kafka) | Criar `DriveDetectionConsumer` | Consumer Kafka que escuta `location.events` e delega para `DriveDetectionProcessingService` |
| 17 | TEST | Testes unitários `DriveDetectionService` | Testar detecção com séries de pontos simulando velocidade |
| 18 | TEST | Testes unitários `DriveDetectionProcessingService` | Testar fluxo completo com mocks |

---

### US-051 – Avaliar comportamento de direção

- Como **responsável por um jovem motorista**, quero **ver uma nota de segurança para cada viagem**, para **avaliar se ele está dirigindo de forma responsável**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DOM | Criar domain service `SafetyScoreCalculator` | Algoritmo básico: score 0-100 baseado em quantidade e severidade de eventos |
| 2 | PIN | Criar interface `GetDrivesUseCase` | Método: execute(UUID userId, GetDrivesInputDto): List\<DriveOutputDto\> |
| 3 | PIN | Criar interface `GetDriveDetailUseCase` | Método: execute(UUID userId, UUID driveId): DriveDetailOutputDto |
| 4 | PIN | Criar `GetDrivesInputDto` | Campos: startDate, endDate |
| 5 | PIN | Criar `DriveOutputDto` | Campos: id, startTime, endTime, distanceMeters, durationSeconds, maxSpeedMps, avgSpeedMps, safetyScore |
| 6 | PIN | Criar `DriveDetailOutputDto` | Campos: drive + List\<DriveEventOutputDto\> |
| 7 | APP | Implementar `GetDrivesService` | Buscar viagens do período para o usuário |
| 8 | APP | Implementar `GetDriveDetailService` | Buscar viagem + eventos de risco |
| 9 | API | Criar interface `DriveControllerApi` | Anotações OpenAPI para `GET /api/v1/drives`, `GET /api/v1/drives/{driveId}` |
| 10 | API | Implementar `DriveController` | Delega para use cases |
| 11 | TEST | Testes unitários `SafetyScoreCalculator` | Testar cálculo com diferentes cenários de eventos |
| 12 | TEST | Testes unitários dos services | Testar consultas |

---

### US-052 – Receber alertas de direção arriscada

- Como **responsável**, quero **receber alertas quando houver direção muito perigosa**, para **agir rapidamente se necessário**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | APP | Atualizar `DriveDetectionProcessingService` | Ao detectar evento de severity HIGH, publicar drive.event com flag de alerta |
| 2 | INF | Atualizar `KafkaDriveEventPublisher` | Incluir campo de severidade no payload do evento Kafka |
| 3 | TEST | Testes unitários | Testar que eventos HIGH geram publicação de alerta |

---

## Épico 7 – SOS e Emergências

### US-060 – Acionar SOS manual

- Como **usuário em perigo**, quero **acionar um botão de SOS**, para **avisar rapidamente minha família com minha localização**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `sos_events` | Tabela `sos_events` com campos: id, user_id (FK), circle_id (FK), trigger_type (ENUM), location_id (FK nullable), message, status (ENUM), started_at, resolved_at, cancelled_at, created_at, updated_at |
| 2 | DOM | Criar entidade `SosEvent` | Factory methods, enum `SosTriggerType` (MANUAL, AUTO_COLLISION_DETECTED), enum `SosStatus` (OPEN, RESOLVED, CANCELLED), métodos de transição de estado |
| 3 | POUT | Criar interface `SosEventRepository` | Métodos: save, findById, findByCircleIdAndStatus, findByUserIdOrderByStartedAtDesc |
| 4 | POUT | Criar interface `SosEventPublisher` | Método: publish(SosEventMessage) — publica no Kafka `sos.events` |
| 5 | PIN | Criar interface `TriggerSosUseCase` | Método: execute(UUID userId, TriggerSosInputDto): SosEventOutputDto |
| 6 | PIN | Criar interface `CancelSosUseCase` | Método: execute(UUID userId, UUID sosEventId): void |
| 7 | PIN | Criar interface `ResolveSosUseCase` | Método: execute(UUID userId, UUID sosEventId): void |
| 8 | PIN | Criar `TriggerSosInputDto` | Campos: circleId, message, latitude, longitude |
| 9 | PIN | Criar `SosEventOutputDto` | Campos: id, userId, circleId, triggerType, status, message, latitude, longitude, startedAt |
| 10 | APP | Implementar `TriggerSosService` | Criar SosEvent, buscar última localização, publicar no Kafka `sos.events` |
| 11 | APP | Implementar `CancelSosService` | Validar que SOS pertence ao usuário, transicionar para CANCELLED |
| 12 | APP | Implementar `ResolveSosService` | Transicionar para RESOLVED |
| 13 | INF | Criar `SosEventJpaEntity` | Mapeamento JPA |
| 14 | INF | Implementar `SosEventJpaRepository` | Implementa `SosEventRepository` |
| 15 | INF | Implementar `KafkaSosEventPublisher` | Publica no tópico `sos.events` particionado por circleId |
| 16 | INF | Configurar tópico Kafka `sos.events` | Configuração de tópico |
| 17 | API | Criar interface `SosControllerApi` | Anotações OpenAPI para `POST /api/v1/sos`, `POST /api/v1/sos/{sosId}/cancel`, `POST /api/v1/sos/{sosId}/resolve` |
| 18 | API | Implementar `SosController` | Delega para use cases |
| 19 | API (Kafka) | Criar `SosBroadcastConsumer` | Consumer Kafka que escuta `sos.events`, busca membros do círculo, envia push/SMS |
| 20 | TEST | Testes unitários `SosEvent` (Domain) | Testar transições de estado (OPEN→RESOLVED, OPEN→CANCELLED) |
| 21 | TEST | Testes unitários dos services | Testar fluxo completo |

---

### US-061 – Ver detalhes de um SOS recebido

- Como **membro do círculo**, quero **ver quem acionou SOS e onde está**, para **decidir como ajudá-lo**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `GetSosEventUseCase` | Método: execute(UUID userId, UUID sosEventId): SosEventDetailOutputDto |
| 2 | PIN | Criar interface `ListSosEventsUseCase` | Método: execute(UUID userId, UUID circleId): List\<SosEventOutputDto\> |
| 3 | PIN | Criar `SosEventDetailOutputDto` | Campos: id, userId, fullName, circleId, circleName, triggerType, status, message, latitude, longitude, startedAt, resolvedAt |
| 4 | APP | Implementar `GetSosEventService` | Buscar SOS + dados do usuário e círculo |
| 5 | APP | Implementar `ListSosEventsService` | Listar SOS do círculo |
| 6 | API | Adicionar endpoints ao `SosController` | `GET /api/v1/sos/{sosId}`, `GET /api/v1/circles/{circleId}/sos` |
| 7 | TEST | Testes unitários dos services | Testar busca e validação de permissão |

---

## Épico 8 – Detecção Automática de Incidentes (Avançado)

### US-070 – Detectar colisão automaticamente

- Como **familiar de um motorista**, quero **ser avisado se uma colisão grave for detectada**, para **poder acionar ajuda rapidamente**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `incident_detections` | Tabela `incident_detections` com campos: id, user_id (FK), drive_id (FK nullable), location_id (FK nullable), confidence, sensor_snapshot (JSON), occurred_at, linked_sos_event_id (FK nullable), created_at |
| 2 | DOM | Criar entidade `IncidentDetection` | Factory methods, campo `confidence` (0-100) |
| 3 | DOM | Criar domain service `CollisionDetectionService` | Lógica básica: desaceleração abrupta extrema → possível colisão |
| 4 | POUT | Criar interface `IncidentDetectionRepository` | Métodos: save, findByUserId |
| 5 | APP | Implementar `IncidentDetectionProcessingService` | Analisar padrão de desaceleração, se confiança > limiar: criar IncidentDetection e acionar SOS automático |
| 6 | INF | Criar `IncidentDetectionJpaEntity` | Mapeamento JPA com campo JSON |
| 7 | INF | Implementar `IncidentDetectionJpaRepository` | Implementa `IncidentDetectionRepository` |
| 8 | TEST | Testes unitários `CollisionDetectionService` | Testar detecção com dados simulados de acelerômetro |

---

## Épico 9 – Comunicação e Check-ins

### US-080 – Conversar no chat do círculo

- Como **membro de um círculo**, quero **enviar mensagens de texto no chat**, para **coordenar com minha família em um só lugar**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `circle_messages` | Tabela `circle_messages` com campos: id, circle_id (FK), sender_user_id (FK), message_text, message_type (ENUM TEXT/SYSTEM), attached_location_id (FK nullable), created_at |
| 2 | DB | Criar migration `circle_message_receipts` | Tabela `circle_message_receipts` com campos: id, message_id (FK), user_id (FK), status (ENUM DELIVERED/READ), updated_at. Constraint UNIQUE(message_id, user_id) |
| 3 | DOM | Criar entidade `CircleMessage` | Factory methods, enum `MessageType` (TEXT, SYSTEM) |
| 4 | DOM | Criar entidade `CircleMessageReceipt` | Factory methods, enum `ReceiptStatus` (DELIVERED, READ) |
| 5 | POUT | Criar interface `CircleMessageRepository` | Métodos: save, findByCircleIdOrderByCreatedAtDesc (paginado) |
| 6 | POUT | Criar interface `CircleMessageReceiptRepository` | Métodos: save, findByMessageId |
| 7 | PIN | Criar interface `SendMessageUseCase` | Método: execute(UUID userId, UUID circleId, SendMessageInputDto): MessageOutputDto |
| 8 | PIN | Criar interface `ListMessagesUseCase` | Método: execute(UUID userId, UUID circleId, Pageable): Page\<MessageOutputDto\> |
| 9 | PIN | Criar interface `MarkMessageReadUseCase` | Método: execute(UUID userId, UUID messageId): void |
| 10 | PIN | Criar DTOs | `SendMessageInputDto`, `MessageOutputDto` |
| 11 | APP | Implementar `SendMessageService` | Validar que usuário pertence ao círculo, verificar se chat está habilitado (circle_settings), criar mensagem |
| 12 | APP | Implementar `ListMessagesService` | Buscar mensagens paginadas do círculo |
| 13 | APP | Implementar `MarkMessageReadService` | Criar/atualizar receipt para READ |
| 14 | INF | Criar `CircleMessageJpaEntity` | Mapeamento JPA |
| 15 | INF | Criar `CircleMessageReceiptJpaEntity` | Mapeamento JPA |
| 16 | INF | Implementar repositórios JPA | `CircleMessageJpaRepository`, `CircleMessageReceiptJpaRepository` |
| 17 | API | Criar interface `ChatControllerApi` | Anotações OpenAPI para `/api/v1/circles/{circleId}/messages` |
| 18 | API | Implementar `ChatController` | Delega para use cases de mensagem |
| 19 | TEST | Testes unitários dos services | Testar envio, listagem e marcação de leitura |

---

### US-081 – Fazer check-in manual

- Como **usuário**, quero **enviar um check-in de que estou bem, com localização**, para **tranquilizar meus familiares**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `checkins` | Tabela `checkins` com campos: id, circle_id (FK), user_id (FK), location_id (FK), message, created_at |
| 2 | DOM | Criar entidade `Checkin` | Factory methods |
| 3 | POUT | Criar interface `CheckinRepository` | Métodos: save, findByCircleIdOrderByCreatedAtDesc |
| 4 | PIN | Criar interface `CreateCheckinUseCase` | Método: execute(UUID userId, UUID circleId, CreateCheckinInputDto): CheckinOutputDto |
| 5 | PIN | Criar DTOs | `CreateCheckinInputDto` (message, latitude, longitude), `CheckinOutputDto` |
| 6 | APP | Implementar `CreateCheckinService` | Criar localização, criar checkin, publicar notificação para membros do círculo |
| 7 | INF | Criar `CheckinJpaEntity` | Mapeamento JPA |
| 8 | INF | Implementar `CheckinJpaRepository` | Implementa `CheckinRepository` |
| 9 | API | Adicionar endpoint ao `ChatController` | `POST /api/v1/circles/{circleId}/checkins` |
| 10 | TEST | Testes unitários `CreateCheckinService` | Testar criação e notificação |

---

## Épico 10 – Notificações e Preferências

### US-090 – Receber notificações de eventos principais

- Como **usuário**, quero **ser avisado sobre eventos relevantes (chegadas, saídas, SOS etc.)**, para **acompanhar a segurança da família**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `notifications` | Tabela `notifications` com campos: id, user_id (FK), circle_id (FK nullable), type (ENUM), title, body, payload (JSON), status (ENUM PENDING/SENT/FAILED), sent_at, created_at |
| 2 | DOM | Criar entidade `Notification` | Factory methods, enum `NotificationType` (PLACE_EVENT, DRIVE_START, DRIVE_END, DRIVE_RISK, SOS, BATTERY_LOW, INVITE, MEMBER_JOINED, MEMBER_LEFT, MEMBER_REMOVED, ADMIN_TRANSFERRED, SYSTEM), enum `NotificationStatus` (PENDING, SENT, FAILED) |
| 3 | POUT | Criar interface `NotificationRepository` | Métodos: save, findByUserIdOrderByCreatedAtDesc (paginado) |
| 4 | POUT | Criar interface `PushNotificationSender` | Método: send(deviceToken, title, body, payload) — abstração FCM/APNS |
| 5 | APP | Implementar `NotificationDispatchService` | Receber evento (geofence, drive, SOS), buscar preferências do usuário, buscar device tokens, enviar push, persistir notificação |
| 6 | INF | Criar `NotificationJpaEntity` | Mapeamento JPA com campo JSON payload |
| 7 | INF | Implementar `NotificationJpaRepository` | Implementa `NotificationRepository` |
| 8 | INF | Implementar `FcmPushNotificationSender` | Implementa `PushNotificationSender` com Firebase Cloud Messaging |
| 9 | API (Kafka) | Criar `NotificationDispatchConsumer` | Consumer Kafka que escuta `geofence.events`, `drive.events`, `notification.commands` e delega para `NotificationDispatchService` |
| 10 | INF | Configurar tópico Kafka `notification.commands` | Tópico genérico para comandos de notificação |
| 11 | PIN | Criar interface `ListNotificationsUseCase` | Método: execute(UUID userId, Pageable): Page\<NotificationOutputDto\> |
| 12 | PIN | Criar `NotificationOutputDto` | Campos: id, type, title, body, circleId, status, sentAt, createdAt |
| 13 | APP | Implementar `ListNotificationsService` | Buscar notificações paginadas do usuário |
| 14 | API | Criar interface `NotificationControllerApi` | Anotações OpenAPI para `GET /api/v1/notifications` |
| 15 | API | Implementar `NotificationController` | Delega para use cases |
| 16 | TEST | Testes unitários `NotificationDispatchService` | Testar roteamento com preferências ativas/inativas |

---

### US-091 – Personalizar quais notificações receber

- Como **usuário**, quero **escolher quais tipos de alerta desejo receber e como**, para **evitar excesso de notificações**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `notification_preferences` | Tabela `notification_preferences` com campos: id, user_id (FK), circle_id (FK nullable), notify_place_events, notify_drives, notify_drive_risk_events, notify_sos, notify_battery_low, notify_invites, sound_mode (ENUM), muted_until, created_at, updated_at. Constraint UNIQUE(user_id, circle_id) |
| 2 | DOM | Criar entidade `NotificationPreference` | Factory methods com defaults (tudo habilitado), enum `SoundMode` (DEFAULT, SILENT, VIBRATE_ONLY) |
| 3 | POUT | Criar interface `NotificationPreferenceRepository` | Métodos: save, findByUserIdAndCircleId, findByUserId |
| 4 | PIN | Criar interface `GetNotificationPreferencesUseCase` | Método: execute(UUID userId, UUID circleId): NotificationPreferenceOutputDto |
| 5 | PIN | Criar interface `UpdateNotificationPreferencesUseCase` | Método: execute(UUID userId, UpdateNotificationPreferenceInputDto): NotificationPreferenceOutputDto |
| 6 | PIN | Criar DTOs | `UpdateNotificationPreferenceInputDto`, `NotificationPreferenceOutputDto` |
| 7 | APP | Implementar `GetNotificationPreferencesService` | Buscar preferências (global ou por círculo) |
| 8 | APP | Implementar `UpdateNotificationPreferencesService` | Criar ou atualizar preferências |
| 9 | INF | Criar `NotificationPreferenceJpaEntity` | Mapeamento JPA |
| 10 | INF | Implementar `NotificationPreferenceJpaRepository` | Implementa `NotificationPreferenceRepository` |
| 11 | API | Adicionar endpoints ao `NotificationController` | `GET /api/v1/notifications/preferences`, `PUT /api/v1/notifications/preferences` |
| 12 | TEST | Testes unitários dos services | Testar defaults, atualização parcial, mute temporário |

---

## Épico 11 – Planos, Assinaturas e Billing

### US-100 – Ver limitações e benefícios do meu plano

- Como **usuário**, quero **ver claramente o que meu plano oferece**, para **entender se preciso migrar para outro plano**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `plans` | Tabela `plans` com campos: id, name, code (unique), description, max_circles, max_places_per_circle, location_history_days, has_advanced_driving_reports, has_incident_detection, price_amount, price_currency, is_active, created_at, updated_at |
| 2 | DB | Criar migration `subscriptions` | Tabela `subscriptions` com campos: id, user_id (FK), plan_id (FK), store (ENUM), store_subscription_id, status (ENUM), start_date, end_date, renewal_date, created_at, updated_at |
| 3 | DB | Migration de dados seed `plans` | Inserir registros FREE e PREMIUM com os limites definidos |
| 4 | DOM | Criar entidade `Plan` | Factory methods, campos de limites |
| 5 | DOM | Criar entidade `Subscription` | Factory methods, enum `Store` (GOOGLE_PLAY, APP_STORE, INTERNAL), enum `SubscriptionStatus` (ACTIVE, CANCELLED, EXPIRED, PENDING) |
| 6 | POUT | Criar interface `PlanRepository` | Métodos: findAll, findByCode, findById |
| 7 | POUT | Criar interface `SubscriptionRepository` (se ainda não existe) | Métodos: save, findActiveByUserId, findByUserId |
| 8 | PIN | Criar interface `ListPlansUseCase` | Método: execute(): List\<PlanOutputDto\> |
| 9 | PIN | Criar interface `GetMySubscriptionUseCase` | Método: execute(UUID userId): SubscriptionOutputDto |
| 10 | PIN | Criar DTOs | `PlanOutputDto`, `SubscriptionOutputDto` |
| 11 | APP | Implementar `ListPlansService` | Buscar planos ativos |
| 12 | APP | Implementar `GetMySubscriptionService` | Buscar assinatura ativa do usuário + detalhes do plano |
| 13 | INF | Criar `PlanJpaEntity` | Mapeamento JPA |
| 14 | INF | Criar `SubscriptionJpaEntity` | Mapeamento JPA |
| 15 | INF | Implementar `PlanJpaRepository` | Implementa `PlanRepository` |
| 16 | INF | Implementar `SubscriptionJpaRepository` | Implementa `SubscriptionRepository` |
| 17 | API | Criar interface `PlanControllerApi` | Anotações OpenAPI para `GET /api/v1/plans`, `GET /api/v1/subscriptions/me` |
| 18 | API | Implementar `PlanController` | Delega para use cases |
| 19 | TEST | Testes unitários dos services | Testar listagem e busca de assinatura |

---

### US-101 – Assinar plano premium

- Como **usuário do plano gratuito**, quero **assinar o plano premium pelo app**, para **ter acesso a mais recursos e histórico maior**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | POUT | Criar interface `StoreBillingAdapter` | Métodos: validateReceipt(store, receiptToken): ReceiptValidationResult |
| 2 | PIN | Criar interface `CreateSubscriptionUseCase` | Método: execute(UUID userId, CreateSubscriptionInputDto): SubscriptionOutputDto |
| 3 | PIN | Criar `CreateSubscriptionInputDto` | Campos: planCode, store, receiptToken |
| 4 | APP | Implementar `CreateSubscriptionService` | Validar recibo na loja, criar/atualizar Subscription, liberar limites premium |
| 5 | INF | Implementar `GooglePlayBillingAdapter` | Implementa `StoreBillingAdapter` para Google Play |
| 6 | INF | Implementar `AppStoreBillingAdapter` | Implementa `StoreBillingAdapter` para Apple App Store |
| 7 | API | Adicionar endpoint ao `PlanController` | `POST /api/v1/subscriptions` |
| 8 | TEST | Testes unitários `CreateSubscriptionService` | Testar validação de recibo mock |

---

## Épico 12 – Privacidade e Segurança de Dados

### US-110 – Controlar onde compartilho minha localização

- Como **usuário preocupado com privacidade**, quero **definir em quais círculos eu compartilho localização e histórico**, para **limitar quem tem acesso às minhas informações**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `UpdateSharingStateUseCase` | Método: execute(UUID userId, UUID circleId, UpdateSharingStateInputDto): SharingStateOutputDto |
| 2 | PIN | Criar interface `GetSharingStatesUseCase` | Método: execute(UUID userId): List\<SharingStateOutputDto\> |
| 3 | PIN | Criar DTOs | `UpdateSharingStateInputDto` (isSharingLocation, isHistoryEnabled), `SharingStateOutputDto` |
| 4 | APP | Implementar `UpdateSharingStateService` | Buscar/criar LocationSharingState, atualizar campos |
| 5 | APP | Implementar `GetSharingStatesService` | Listar estados de compartilhamento de todos os círculos do usuário |
| 6 | API | Adicionar endpoints ao `LocationController` | `PUT /api/v1/circles/{circleId}/sharing-state`, `GET /api/v1/sharing-states` |
| 7 | TEST | Testes unitários dos services | Testar ativação/desativação por círculo |

---

### US-111 – Saber quem pode me ver

- Como **usuário**, quero **ver quem tem acesso à minha localização**, para **tomar decisões conscientes sobre privacidade**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `GetPrivacySummaryUseCase` | Método: execute(UUID userId): PrivacySummaryOutputDto |
| 2 | PIN | Criar `PrivacySummaryOutputDto` | Campos: List\<CirclePrivacyDto\> (circleId, circleName, isSharingLocation, isHistoryEnabled, memberCount) |
| 3 | APP | Implementar `GetPrivacySummaryService` | Agregar dados de todos os círculos + estados de compartilhamento |
| 4 | API | Adicionar endpoint ao `UserController` | `GET /api/v1/users/me/privacy` |
| 5 | TEST | Testes unitários `GetPrivacySummaryService` | Testar agregação com múltiplos círculos |

---

## Épico 13 – Administração e Suporte (Backoffice)

### US-120 – Consultar usuário no painel administrativo

- Como **analista de suporte**, quero **buscar um usuário por e-mail/telefone/ID**, para **ver status de conta, círculos e plano**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `admin_users` | Tabela `admin_users` com campos: id, email (unique), full_name, password_hash, role (ENUM SUPPORT/ADMIN/SUPER_ADMIN), status (ENUM), created_at, updated_at |
| 2 | DOM | Criar entidade `AdminUser` | Factory methods, enum `AdminRole` (SUPPORT, ADMIN, SUPER_ADMIN) |
| 3 | POUT | Criar interface `AdminUserRepository` | Métodos: findByEmail, findById |
| 4 | PIN | Criar interface `SearchUsersAdminUseCase` | Método: execute(SearchUsersAdminInputDto): Page\<AdminUserViewOutputDto\> |
| 5 | PIN | Criar `SearchUsersAdminInputDto` | Campos: email, phoneNumber, userId, page, size |
| 6 | PIN | Criar `AdminUserViewOutputDto` | Campos: id, email, phone, fullName, status, planCode, circlesCount, createdAt |
| 7 | APP | Implementar `SearchUsersAdminService` | Buscar usuário por múltiplos critérios, agregar dados de plano e círculos |
| 8 | INF | Criar `AdminUserJpaEntity` | Mapeamento JPA |
| 9 | INF | Implementar `AdminUserJpaRepository` | Implementa `AdminUserRepository` |
| 10 | API | Criar interface `AdminControllerApi` | Anotações OpenAPI para `GET /api/v1/admin/users` |
| 11 | API | Implementar `AdminController` | Delega para use cases admin, protegido por role SUPPORT+ |
| 12 | INF | Configurar Spring Security para rotas admin | Filtro separado ou role-based access para `/api/v1/admin/**` |
| 13 | TEST | Testes unitários `SearchUsersAdminService` | Testar busca por diferentes critérios |

---

### US-121 – Bloquear conta em caso de abuso

- Como **administrador da plataforma**, quero **bloquear a conta de um usuário**, para **evitar uso indevido do serviço**.

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `BlockUserAdminUseCase` | Método: execute(UUID adminId, UUID userId): void |
| 2 | PIN | Criar interface `UnblockUserAdminUseCase` | Método: execute(UUID adminId, UUID userId): void |
| 3 | APP | Implementar `BlockUserAdminService` | Alterar status do User para BLOCKED, revogar todas as sessões ativas, criar audit log |
| 4 | APP | Implementar `UnblockUserAdminService` | Alterar status para ACTIVE, criar audit log |
| 5 | API | Adicionar endpoints ao `AdminController` | `POST /api/v1/admin/users/{userId}/block`, `POST /api/v1/admin/users/{userId}/unblock` |
| 6 | TEST | Testes unitários dos services | Testar bloqueio, desbloqueio e criação de audit log |

---

## Histórias Técnicas de Aderência ao Modelo de Dados

### US-130 – Gerenciar identidades de autenticação por usuário

> Coberta pelas tarefas da **US-001** (AuthIdentity já implementada no cadastro).

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `ListAuthIdentitiesUseCase` | Método: execute(UUID userId): List\<AuthIdentityOutputDto\> |
| 2 | PIN | Criar interface `LinkAuthIdentityUseCase` | Método: execute(UUID userId, LinkAuthIdentityInputDto): AuthIdentityOutputDto |
| 3 | PIN | Criar DTOs | `AuthIdentityOutputDto`, `LinkAuthIdentityInputDto` |
| 4 | APP | Implementar `ListAuthIdentitiesService` | Listar identidades vinculadas à conta |
| 5 | APP | Implementar `LinkAuthIdentityService` | Adicionar nova identidade (ex: vincular Google a conta existente) |
| 6 | API | Adicionar endpoints ao `AuthController` | `GET /api/v1/auth/identities`, `POST /api/v1/auth/identities/link` |
| 7 | TEST | Testes unitários dos services | Testar listagem e vinculação |

---

### US-131 – Controlar tokens de verificação e recuperação

> Coberta parcialmente pelas tarefas da **US-001** e **US-003** (VerificationToken já implementada).

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `VerifyAccountUseCase` | Método: execute(VerifyAccountInputDto): void |
| 2 | PIN | Criar `VerifyAccountInputDto` | Campos: token |
| 3 | APP | Implementar `VerifyAccountService` | Validar token (tipo, expiração, uso), marcar AuthIdentity como verificada, marcar token como usado |
| 4 | API | Adicionar endpoint ao `AuthController` | `POST /api/v1/auth/verify` |
| 5 | TEST | Testes unitários `VerifyAccountService` | Testar token válido, expirado, já usado |

---

### US-132 – Gerenciar dispositivos ativos e revogação de sessão

> Coberta parcialmente pelas tarefas da **US-002** (Device já criada no login).

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | PIN | Criar interface `ListDevicesUseCase` | Método: execute(UUID userId): List\<DeviceOutputDto\> |
| 2 | PIN | Criar interface `RevokeDeviceUseCase` | Método: execute(UUID userId, UUID deviceId): void |
| 3 | PIN | Criar `DeviceOutputDto` | Campos: id, platform, deviceModel, osVersion, appVersion, isActive, lastSeenAt |
| 4 | APP | Implementar `ListDevicesService` | Listar dispositivos ativos do usuário |
| 5 | APP | Implementar `RevokeDeviceService` | Desativar dispositivo e revogar tokens associados |
| 6 | API | Adicionar endpoints ao `UserController` | `GET /api/v1/users/me/devices`, `DELETE /api/v1/users/me/devices/{deviceId}` |
| 7 | TEST | Testes unitários dos services | Testar listagem e revogação |

---

### US-133 – Configurar políticas de alerta por lugar

> Coberta pelas tarefas da **US-032** (PlaceAlertPolicy já implementada).

Sem tarefas adicionais.

---

### US-134 – Persistir estado de compartilhamento por usuário e círculo

> Coberta pelas tarefas da **US-022** e **US-110** (LocationSharingState já implementada).

Sem tarefas adicionais.

---

### US-135 – Registrar marcações administrativas em contas

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `user_flags` | Tabela `user_flags` com campos: id, user_id (FK), flag_type (ENUM ABUSE_REPORT/FRAUD_SUSPECT/OTHER), notes, created_by_admin_id (FK), created_at |
| 2 | DOM | Criar entidade `UserFlag` | Factory methods, enum `FlagType` (ABUSE_REPORT, FRAUD_SUSPECT, OTHER) |
| 3 | POUT | Criar interface `UserFlagRepository` | Métodos: save, findByUserId |
| 4 | PIN | Criar interface `CreateUserFlagUseCase` | Método: execute(UUID adminId, CreateUserFlagInputDto): UserFlagOutputDto |
| 5 | PIN | Criar interface `ListUserFlagsUseCase` | Método: execute(UUID userId): List\<UserFlagOutputDto\> |
| 6 | PIN | Criar DTOs | `CreateUserFlagInputDto`, `UserFlagOutputDto` |
| 7 | APP | Implementar `CreateUserFlagService` | Criar flag com referência ao admin |
| 8 | APP | Implementar `ListUserFlagsService` | Listar flags de um usuário |
| 9 | INF | Criar `UserFlagJpaEntity` | Mapeamento JPA |
| 10 | INF | Implementar `UserFlagJpaRepository` | Implementa `UserFlagRepository` |
| 11 | API | Adicionar endpoints ao `AdminController` | `POST /api/v1/admin/users/{userId}/flags`, `GET /api/v1/admin/users/{userId}/flags` |
| 12 | TEST | Testes unitários dos services | Testar criação e listagem de flags |

---

### US-136 – Auditar ações sensíveis do backoffice

| # | Camada | Tarefa | Detalhes |
|---|--------|--------|----------|
| 1 | DB | Criar migration `audit_logs` | Tabela `audit_logs` com campos: id, admin_user_id (FK), action, target_type, target_id, metadata (JSON), created_at |
| 2 | DOM | Criar entidade `AuditLog` | Factory methods |
| 3 | POUT | Criar interface `AuditLogRepository` | Métodos: save, findByTargetTypeAndTargetId, findByAdminUserId (paginado) |
| 4 | PIN | Criar interface `ListAuditLogsUseCase` | Método: execute(ListAuditLogsInputDto): Page\<AuditLogOutputDto\> |
| 5 | PIN | Criar DTOs | `ListAuditLogsInputDto` (targetType, targetId, adminUserId, page, size), `AuditLogOutputDto` |
| 6 | APP | Implementar `AuditLogService` | Método de criação (chamado pelos outros admin services) e listagem |
| 7 | INF | Criar `AuditLogJpaEntity` | Mapeamento JPA com campo JSON |
| 8 | INF | Implementar `AuditLogJpaRepository` | Implementa `AuditLogRepository` |
| 9 | API | Adicionar endpoints ao `AdminController` | `GET /api/v1/admin/audit-logs` |
| 10 | TEST | Testes unitários `AuditLogService` | Testar criação e filtros de consulta |

---

## Resumo Quantitativo

| Épico | US | Total de Tarefas |
|-------|---:|:----------------:|
| 1 – Gestão de Conta e Acesso | 4 | 65 |
| 2 – Círculos | 5 | 47 |
| 3 – Localização em Tempo Real | 3 | 34 |
| 4 – Lugares & Geofences | 3 | 45 |
| 5 – Histórico de Localização | 2 | 11 |
| 6 – Direção & Segurança | 3 | 33 |
| 7 – SOS & Emergências | 2 | 28 |
| 8 – Detecção de Incidentes | 1 | 8 |
| 9 – Comunicação & Check-ins | 2 | 29 |
| 10 – Notificações | 2 | 28 |
| 11 – Planos & Billing | 2 | 27 |
| 12 – Privacidade | 2 | 12 |
| 13 – Admin & Suporte | 2 | 19 |
| Técnicas (A/B/C) | 5 | 32 |
| **TOTAL** | **36** | **418** |
