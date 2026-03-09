# Guia de Uso da API

Este guia descreve como os clientes (principalmente apps mobile) devem usar a API definida em `openapi.yaml`.

- Especificação OpenAPI: `Docs/openapi.yaml`
- Autenticação: Bearer JWT – header `Authorization: Bearer <accessToken>`
- Formato: JSON em todas as requisições/respostas (salvo erro explícito)

---

## 1. Fluxo de Onboarding e Autenticação

### 1.1 Registro de Usuário

**Objetivo:** Criar uma conta e receber tokens para iniciar a sessão.

**Endpoints principais:**

- `POST /auth/register`

**Caminhos suportados:**

- Registro com e-mail/senha: corpo `RegisterWithEmailRequest` (email, password, fullName).
- Registro com telefone/código: corpo `RegisterWithPhoneRequest` (phoneNumber, verificationCode, fullName).

**Resposta esperada (201):**

- `AuthResponse`: contém `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `user`.

### 1.2 Login (Sessão)

**Endpoint:**

- `POST /auth/login`

**Formas de login:**

- E-mail/senha: `LoginWithPasswordRequest`.
- Telefone/código: `LoginWithPhoneRequest`.

**Resposta (200):**

- `AuthResponse` (mesmo formato do registro).

### 1.3 Obter e Atualizar Perfil

**Obter dados do usuário autenticado:**

- `GET /auth/me` ou `GET /users/me` → `User`.

**Atualizar perfil:**

- `PATCH /users/me`
- Corpo: `UpdateUserProfileRequest` (campos opcionais).

### 1.4 Identidades, Verificação e Dispositivos

Para refletir o domínio de autenticação completo (`auth_identities`, `verification_tokens`, `devices`), a API deve contemplar também:

- **Verificação de contato:**
  - `POST /auth/verification/request` (envio de código por e-mail/SMS).
  - `POST /auth/verification/confirm` (confirmação de código/token).
- **Recuperação de senha:**
  - `POST /auth/password/forgot`
  - `POST /auth/password/reset`
- **Gestão de dispositivos/sessões:**
  - `GET /devices/me`
  - `PATCH /devices/{deviceId}` (ativar/desativar push token)
  - `DELETE /devices/{deviceId}` (revogar sessão/dispositivo)

> Observação: se algum endpoint acima ainda não estiver no `openapi.yaml`, considerar como backlog técnico para manter aderência ao modelo de dados.

---

## 2. Gestão de Círculos

### 2.1 Listar e Criar Círculos

**Listar círculos do usuário:**

- `GET /circles` → lista de `Circle` (cada item indica o `role` do usuário).

**Criar novo círculo:**

- `POST /circles`
- Corpo: `CreateCircleRequest` (nome obrigatório, demais campos opcionais).
- Resposta (201): `Circle` com o usuário criador como `ADMIN`.

### 2.2 Detalhar, Atualizar e Excluir Círculo

**Detalhar círculo:**

- `GET /circles/{circleId}` → `Circle`.

**Atualizar círculo (admin):**

- `PATCH /circles/{circleId}`
- Corpo: `UpdateCircleRequest` (nome, foto, cor, nível de privacidade).

**Excluir círculo (admin):**

- `DELETE /circles/{circleId}` → 204 em caso de sucesso.

### 2.3 Membros do Círculo

**Listar membros:**

- `GET /circles/{circleId}/members` → lista de `CircleMember` (userId, fullName, role, status, joinedAt).

**Entrar com código de convite:**

- `POST /circles/{circleId}/join`
- Corpo: `{ "inviteCode": "..." }`.
- Resposta (200): `Circle` atualizado, incluindo o usuário como membro.

### 2.4 Convites de Círculo

Para refletir `circle_invites`, além do fluxo de `join`:

- `POST /circles/{circleId}/invites` (gerar convite por código/link ou alvo explícito)
- `GET /circles/{circleId}/invites` (listar convites com status)
- `PATCH /circles/{circleId}/invites/{inviteId}` (cancelar/expirar convite)
- `POST /circles/invites/{inviteCode}/accept` (aceitar convite)

Campos de negócio esperados: `inviteCode`, `targetEmail`/`targetPhone`, `status`, `expiresAt`, `acceptedByUserId`.

### 2.5 Configurações do Círculo

Para refletir `circle_settings`:

- `GET /circles/{circleId}/settings` → `CircleSettings`
- `PUT /circles/{circleId}/settings` → `CircleSettings`

Exemplos de campos: `drivingAlertLevel`, `allowMemberChat`, `allowMemberSos`.

---

## 3. Localização em Tempo Real e Histórico

### 3.1 Envio de Localização (Mobile → Backend)

**Endpoint:**

- `POST /locations/stream`

**Uso típico no app:**

- O app acumula múltiplos pontos de localização e envia em lote.

**Corpo esperado:**

- `circleId` (opcional): círculo de contexto.
- `events`: array de `LocationEventInput` (latitude, longitude, source, recordedAt, etc.).

**Resposta:**

- `202 Accepted` indicando que os eventos foram aceitos para processamento.

### 3.2 Última Localização de um Membro

**Endpoint:**

- `GET /circles/{circleId}/members/{memberId}/location`

**Uso típico:**

- Tela de mapa: ao exibir posição em tempo real, buscar a última localização conhecida de cada membro.

**Resposta (200):**

- `Location` (id, userId, latitude, longitude, recordedAt, etc.).

### 3.3 Histórico de Localização

**Endpoint:**

- `GET /circles/{circleId}/members/{memberId}/locations/history?start=...&end=...`

**Uso típico:**

- Tela de "Linha do tempo" ou "Onde esteve hoje":
  - `start` e `end` em ISO 8601.

**Resposta (200):**

- Lista de `Location` no intervalo solicitado.

### 3.4 Estado de Compartilhamento por Círculo

Para refletir `location_sharing_states` (privacidade por círculo):

- `GET /circles/{circleId}/sharing/me` → estado atual (`isSharingLocation`, `isHistoryEnabled`, `pausedUntil`)
- `PUT /circles/{circleId}/sharing/me` → atualiza compartilhamento/histórico

Esse estado governa se o backend deve expor última localização e histórico do usuário naquele círculo.

---

## 4. Lugares (Places) e Geofences

### 4.1 Gestão de Lugares

**Listar lugares do círculo:**

- `GET /circles/{circleId}/places` → `[Place]`.

**Criar lugar:**

- `POST /circles/{circleId}/places`
- Corpo: `CreatePlaceRequest` (name, latitude, longitude, radiusMeters, tipo opcional).
- Resposta (201): `Place`.

**Detalhar, atualizar, excluir:**

- `GET /circles/{circleId}/places/{placeId}` → `Place`.
- `PATCH /circles/{circleId}/places/{placeId}` → `Place`.
- `DELETE /circles/{circleId}/places/{placeId}` → 204.

### 4.2 Alertas de Entrada/Saída (Comportamento)

- O backend detecta eventos com base em `locations` e geofences.
- Para refletir `place_alert_policies`, `place_alert_targets` e `place_events`, recomenda-se:
  - `GET /circles/{circleId}/places/{placeId}/alert-policies`
  - `PUT /circles/{circleId}/places/{placeId}/alert-policies`
  - `GET /circles/{circleId}/places/events?start=...&end=...&memberId=...`

Campos de política esperados: `alertOnEnter`, `alertOnExit`, `daysOfWeek`, `startTime`, `endTime`, `targetType`, `targetUserIds`.

---

## 5. Direção (Drives) e Relatórios

Nomenclatura adotada neste projeto: `drive` para viagens e `drive_event` para eventos de direção.

Escopo desta versão: as funcionalidades de direção consideram apenas viagens com `mode = CAR`.

### 5.1 Listar Viagens de um Membro

**Endpoint:**

- `GET /circles/{circleId}/members/{memberId}/drives?start=...&end=...`

**Uso:**

- Tela de histórico de direção de um membro.

**Resposta (200):**

- Lista de `Drive` (início/fim, distância, duração, velocidade média/máx., safetyScore).

### 5.2 Detalhes de uma Viagem

**Endpoint principal:**

- `GET /drives/{driveId}?includeEvents=true`

**Uso:**

- Tela de detalhes da viagem (rota + eventos de direção).

**Resposta (200):**

- `Drive` com `events` (opcional) do tipo `DriveEvent`.

**Listagem isolada de eventos:**

- `GET /drives/{driveId}/events` → `[DriveEvent]`.

### 5.3 Resumo de Direção Segura

**Endpoint:**

- `GET /circles/{circleId}/members/{memberId}/driving/summary?start=...&end=...`

**Uso:**

- Tela de "score" de direção por semana ou mês.

**Resposta (200):**

- `DrivingSummary`: total de viagens, distância total, duração total, média de score de segurança, número de eventos de alto risco.

---

## 6. Chat de Círculo e Check-ins

### 6.1 Chat do Círculo

**Listar mensagens recentes:**

- `GET /circles/{circleId}/messages?since=...` → `[CircleMessage]`.

**Enviar mensagem:**

- `POST /circles/{circleId}/messages`
- Corpo: `CreateCircleMessageRequest` (messageText obrigatório, attachedLocation opcional).
- Resposta (201): `CircleMessage` criada.

**Recibos de leitura/entrega:**

- `GET /circles/{circleId}/messages/{messageId}/receipts` → `[CircleMessageReceipt]`.

*(Marcar como lido/entregue pode ser implementado depois com endpoints extras, se necessário.)*

### 6.2 Check-ins Manuais

**Listar check-ins do círculo:**

- `GET /circles/{circleId}/checkins?start=...&end=...` → `[Checkin]`.

**Criar check-in:**

- `POST /circles/{circleId}/checkins`
- Corpo: `CreateCheckinRequest` (message, location).
- Resposta (201): `Checkin`.

---

## 7. SOS (Emergências)

### 7.1 Acionar SOS

**Endpoint:**

- `POST /sos`

**Uso:**

- Botão SOS no app: ao confirmar, envia requisição.

**Corpo:**

- `CreateSosRequest` (circleId obrigatório, message opcional, location opcional).

**Resposta (201):**

- `SosEvent` (id, userId, circleId, triggerType, status, startedAt, etc.).

### 7.2 Atualizar Status do SOS

**Endpoint:**

- `PATCH /sos/{sosId}`

**Uso:**

- Marcar SOS como resolvido ou cancelado.

**Corpo:**

- `UpdateSosStatusRequest` (status = RESOLVED ou CANCELLED).

**Resposta (200):**

- `SosEvent` atualizado.

### 7.3 Incidentes Detectados Automaticamente

Para refletir `incident_detections`:

- `GET /incidents?memberId=...&start=...&end=...`
- `GET /incidents/{incidentId}`

Campos esperados: `confidence`, `occurredAt`, `driveId`, `location`, `linkedSosEventId`.

---

## 8. Notificações e Preferências

### 8.1 Preferências de Notificação

**Obter preferências:**

- `GET /notification-preferences` →
  - Pode retornar uma lista de `NotificationPreferences` (global + por círculo).
- `GET /notification-preferences?circleId=...` → preferência específica.

**Atualizar preferências:**

- `PUT /notification-preferences`
- Corpo: `UpdateNotificationPreferencesRequest` (circleId opcional, flags de tipos de alerta, soundMode, mutedUntil).

### 8.2 Histórico de Notificações

**Endpoint:**

- `GET /notifications?type=...&status=...&since=...`

**Uso:**

- Tela de "central de notificações" opcional.

**Resposta:**

- Lista de `Notification` (tipo, título, corpo, payload, status, createdAt).

---

## 9. Planos e Assinaturas

### 9.1 Planos Disponíveis

**Listar planos:**

- `GET /plans` → `[Plan]`.

**Detalhar plano:**

- `GET /plans/{planId}` → `Plan`.

### 9.2 Assinaturas do Usuário

**Obter assinaturas do usuário atual:**

- `GET /subscriptions/me` → `[Subscription]` (plano atual e histórico).

**Criar/registrar assinatura:**

- `POST /subscriptions`
- Corpo: `CreateSubscriptionRequest` (planId, store, storeSubscriptionId opcional).
- Uso típico: chamado após sucesso da compra in-app, para sincronizar servidor.

---

## 10. Boas Práticas de Integração

- Sempre enviar e interpretar datas no formato ISO 8601 (ex.: `2026-02-10T13:45:00Z`).
- Utilizar HTTPS em todos os ambientes.
- Tratar respostas de erro padronizadas (`ErrorResponse`) exibindo mensagens adequadas ao usuário.
- Minimizar a frequência de chamadas de localização, preferindo `/locations/stream` em lote.
- Respeitar configurações de privacidade do usuário (por exemplo, não exibir histórico quando desativado no app).

---

## 11. Administração e Suporte (Backoffice)

Para refletir `admin_users`, `user_flags` e `audit_logs`, a API administrativa (separada da API mobile) deve expor:

- `GET /admin/users?query=...` (busca por e-mail/telefone/ID)
- `PATCH /admin/users/{userId}/status` (bloquear/desbloquear)
- `POST /admin/users/{userId}/flags` (marcação de abuso/fraude)
- `GET /admin/audit-logs?targetType=...&targetId=...`

Recomendação: manter escopo administrativo em autenticação e autorização segregadas (`SUPPORT`, `ADMIN`, `SUPER_ADMIN`).
