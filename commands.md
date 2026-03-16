# Locator 360 — Comandos de Desenvolvimento

## 1. Subir a infraestrutura (PostgreSQL, Redis, Kafka, Zookeeper, Grafana LGTM)

```powershell
docker compose -f docker-compose.infra.yml up -d
```

Aguarde todos os containers ficarem _Healthy_ antes de prosseguir.

## 2. Verificar containers em execução

```powershell
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String "fl-"
```

Resultado esperado (todos healthy):

fl-kafka-ui    Up 15 minutes             0.0.0.0:8090->8080/tcp, [::]:8090->8080/tcp
fl-kafka       Up 15 minutes (healthy)   0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp
fl-redis       Up 15 minutes (healthy)   0.0.0.0:6379->6379/tcp, [::]:6379->6379/tcp
fl-redis-commander Up 15 minutes         0.0.0.0:8081->8081/tcp, [::]:8081->8081/tcp
fl-otel-lgtm   Up 15 minutes             0.0.0.0:3000->3000/tcp, [::]:3000->3000/tcp,
0.0.0.0:4317-4318->4317-4318/tcp, [::]:4317-4318->4317-4318/tcp, 0.0.0.0:9090->9090/tcp, [::]:9090->9090/tcp
fl-zookeeper   Up 15 minutes (healthy)   2181/tcp, 2888/tcp, 3888/tcp
fl-postgres    Up 15 minutes (healthy)   0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp

## 2.1 Redis GUI (Redis Commander)

```powershell
docker run --rm -d --name redis-commander -p 8081:8081 `
  --env REDIS_HOSTS=local:host.docker.internal:6379 `
  rediscommander/redis-commander:latest
```

```text
http://localhost:8081
```

> Linux: se `host.docker.internal` não resolver, substitua pelo host/IP do Docker.

## 3. Executar a aplicação Spring Boot (via Docker Maven)

> **Pré-requisito:** a infra deve estar rodando (passo 1).
> O Maven não está instalado localmente; usamos a imagem Docker `maven:3.9.9-eclipse-temurin-17`.
> A aplicação se conecta aos containers de infra pela rede Docker `fl-network`.

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  --network fl-network -p 8080:8080 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/family_locator `
  -e SPRING_DATA_REDIS_HOST=redis `
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 `
  -e MANAGEMENT_OTLP_TRACING_ENDPOINT=http://otel-lgtm:4318/v1/traces `
  -e MANAGEMENT_OTLP_METRICS_ENDPOINT=http://otel-lgtm:4318/v1/metrics `
  -e LOKI_HOST=otel-lgtm `
  maven:3.9.9-eclipse-temurin-17 mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## 3.1 Acessar o Grafana LGTM (logs, métricas e traces)

```
http://localhost:3000
# (admin/admin)
# Explore → Loki, Tempo, ou Prometheus
```

A aplicação estará disponível em `http://localhost:8080` quando aparecer no log:

```
Started FamilyLocatorApplication in X seconds
```

## 4. Executar os testes

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 mvn test "-Dsurefire.useFile=false"
```

## 5. Compilar sem testes

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 mvn -DskipTests compile
```

## 6. Workflow Git — Small PR por Commit

> **Regra**: cada commit atômico gera **1 branch + 1 PR**. Usa `--auto` para merge após CI.

### Passo a passo (por commit)

```powershell
# 1. Criar branch a partir de main atualizada
git checkout main
git pull origin main
git checkout -b <type>/<scope>-<descricao-curta>

# 2. Implementar, stage e commit
git add <arquivos>
git commit -m "<type>(<scope>): <description>"

# 3. Push e criar PR com auto-merge
git push -u origin HEAD
gh pr create --base main --head (git branch --show-current) `
  --title "<type>(<scope>): <description>"

# 3.1 Preencher o checklist obrigatório de code review
# Template oficial: .github/pull_request_template.md

gh pr merge (git branch --show-current) --auto --rebase --delete-branch

# 4. Voltar para main e aguardar merge
git checkout main
# (aguardar CI finalizar — auto-merge acontece automaticamente)
git pull origin main
```

### Convenção de nomes de branch

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Feature | `feat/<scope>-<desc>` | `feat/auth-login-service` |
| Fix | `fix/<scope>-<desc>` | `fix/circle-duplicate-invite` |
| Test | `test/<scope>-<desc>` | `test/auth-login-tests` |
| Migration | `migration/<scope>-<desc>` | `migration/auth-devices-table` |
| Refactor | `refactor/<scope>-<desc>` | `refactor/location-geofence` |
| Chore | `chore/<desc>` | `chore/add-prometheus-dep` |
| Style | `style/<scope>-<desc>` | `style/auth-formatting` |

### Verificar status do PR (enquanto espera CI)

```powershell
gh pr status
```

### Forçar pull após merge automático

```powershell
git checkout main; git pull origin main
```

## 7. Testar os endpoints

### US-001: Registro com email

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register/email" `
  -Method POST -ContentType "application/json" `
  -Body '{"email":"maria@example.com","password":"SenhaForte123!","fullName":"Maria Oliveira"}'
```

### US-001: Registro com telefone

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register/phone" `
  -Method POST -ContentType "application/json" `
  -Body '{"phoneNumber":"+5511999999999","verificationCode":"123456","fullName":"Ana Santos"}'
```

### US-002: login com email

```powershell
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login/email" `
  -Method POST -ContentType "application/json" `
  -Body '{"email":"rodrigo.romanzini@gmail.com","password":"SenhaForte123!"}'

$login | ConvertTo-Json -Depth 5
$accessToken = $login.accessToken
$refreshToken = $login.refreshToken
```

### US-002: login com telefone

```powershell
$phoneLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login/phone" `
  -Method POST -ContentType "application/json" `
  -Body '{"phoneNumber":"+5511999999999","verificationCode":"123456"}'

$phoneLogin | ConvertTo-Json -Depth 5
```

### US-002: refresh token

```powershell
$refresh = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/refresh" `
  -Method POST -ContentType "application/json" `
  -Body "{""refreshToken"":""$refreshToken""}"

$refresh | ConvertTo-Json -Depth 5
```

### US-002: logout

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/logout" `
  -Method POST `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -StatusCodeVariable status

$status
```

### US-003: solicitar recuperação de senha por email

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/password-reset/request" `
  -Method POST -ContentType "application/json" `
  -Body '{"email":"maria@example.com"}' `
  -StatusCodeVariable status

$status
```

### US-003: solicitar recuperação de senha por telefone

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/password-reset/request" `
  -Method POST -ContentType "application/json" `
  -Body '{"phoneNumber":"+5511999999999"}' `
  -StatusCodeVariable status

$status
```

### US-003: confirmar recuperação de senha

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/password-reset/confirm" `
  -Method POST -ContentType "application/json" `
  -Body '{"token":"reset-token-123","newPassword":"NovaSenha123!"}' `
  -StatusCodeVariable status

$status
```

### US-002: rodar testes automatizados do pacote

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=AuthControllerTest,LoginServiceTest,RefreshTokenServiceTest,LogoutServiceTest,AuthenticationServiceTest,JwtTokenProviderTest,DeviceTest,DeviceJpaRepositoryAdapterTest" `
  test "-Dsurefire.useFile=false"
```

### US-003: rodar testes automatizados do pacote

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=AuthIdentityTest,RequestPasswordResetServiceTest,ConfirmPasswordResetServiceTest,AuthControllerTest" `
  test "-Dsurefire.useFile=false"
```

### US-004: buscar perfil do usuário autenticado

```powershell
# Primeiro faça login para obter o token (veja US-002 acima)
$profile = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/me" `
  -Method GET `
  -Headers @{ Authorization = "Bearer $accessToken" }

$profile | ConvertTo-Json -Depth 5
```

### US-004: atualizar perfil (PATCH parcial)

```powershell
# Atualizar nome e timezone
$updatedProfile = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/me" `
  -Method PATCH -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{"fullName":"Maria Oliveira Silva","timezone":"America/Sao_Paulo"}'

$updatedProfile | ConvertTo-Json -Depth 5
```

```powershell
# Atualizar apenas foto de perfil
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/me" `
  -Method PATCH -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{"profilePhotoUrl":"https://example.com/foto.jpg"}'
```

```powershell
# Atualizar múltiplos campos
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/me" `
  -Method PATCH -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{
    "fullName": "Maria Oliveira",
    "birthDate": "1990-05-15",
    "gender": "female",
    "preferredLanguage": "pt-BR",
    "timezone": "America/Sao_Paulo",
    "distanceUnit": "KILOMETERS"
  }'
```

### US-004: rodar testes automatizados do pacote

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=UserTest,GetUserProfileServiceTest,UpdateUserProfileServiceTest,UserControllerTest" `
  test "-Dsurefire.useFile=false"
```

### US-010: criar círculo

```powershell
# Primeiro faça login para obter o token (veja US-002 acima)
$circle = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{
    "name": "Família Silva",
    "description": "Grupo da família",
    "photoUrl": "https://example.com/family.jpg",
    "colorHex": "#4CAF50",
    "privacyLevel": "INVITE_ONLY"
  }'

$circle | ConvertTo-Json -Depth 5
```

```powershell
# Criar círculo com campos mínimos (apenas nome)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{"name": "Meu Círculo"}'
```

### US-010: rodar testes automatizados do pacote

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=CircleTest,CircleMemberTest,CircleSettingsTest,CreateCircleServiceTest,CircleControllerTest" `
  test "-Dsurefire.useFile=false"
```

### US-011: Convidar pessoas para o círculo

```powershell
# Primeiro faça login para obter o token (veja US-002 acima)
$invite = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/{circleId}/invites" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{
    "targetEmail":"juliana.romanzini@gmail.com"
  }'

$invite | ConvertTo-Json -Depth 5
```

### US-012: Entrar em círculo com código/link

```powershell
# Usa o inviteCode retornado pelo US-011 acima
$member = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/join" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body "{`"inviteCode`":`"$($invite.inviteCode)`"}"

$member | ConvertTo-Json -Depth 5
```

### US-013: Listar membros do círculo

```powershell
# Usa circleId retornado pelo US-010
$members = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/$($circle.id)/members" `
  -Method GET `
  -Headers @{ Authorization = "Bearer $accessToken" }

$members | ConvertTo-Json -Depth 5
```

### US-013: Remover membro do círculo (admin only)

```powershell
# Usa circleId e memberId (id do CircleMember, não userId)
$memberId = $members[1].id
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/$($circle.id)/members/$memberId" `
  -Method DELETE `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -StatusCodeVariable status
$status
```

### US-013: Transferir administração (admin only)

```powershell
# Transfere a role de ADMIN para outro membro ativo
$newAdminMemberId = $members[1].id
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/$($circle.id)/members/$newAdminMemberId/transfer-admin" `
  -Method PUT `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -StatusCodeVariable status
$status
```

### US-014: Sair de um círculo

```powershell
# Usa circleId retornado pelo US-010
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/1385a658-6fd5-4f97-a081-703a27556fef/leave" `
  -Method POST `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -StatusCodeVariable status
$status
```

### US-020: Compartilhar localização com o círculo

```powershell
# Primeiro faça login para obter o token (veja US-002 acima)
# Enviar lote de eventos de localização (retorna 202 Accepted)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations/stream" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{
    "circleId": "REPLACE_WITH_CIRCLE_ID",
    "events": [
      {
        "latitude": -23.561414,
        "longitude": -46.655881,
        "accuracyMeters": 10.5,
        "speedMps": 0,
        "headingDegrees": 180,
        "altitudeMeters": 760,
        "source": "GPS",
        "recordedAt": "2026-03-16T13:44:30Z",
        "isMoving": false,
        "batteryLevel": 72
      },
      {
        "latitude": -23.561500,
        "longitude": -46.655900,
        "accuracyMeters": 11.0,
        "speedMps": 1.2,
        "headingDegrees": 182,
        "altitudeMeters": 761,
        "source": "GPS",
        "recordedAt": "2026-03-16T13:44:45Z",
        "isMoving": true,
        "batteryLevel": 72
      }
    ]
  }' -StatusCodeVariable status

$status
```

```powershell
# Enviar eventos sem circleId (contexto geral)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations/stream" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{
    "events": [
      {
        "latitude": -23.561414,
        "longitude": -46.655881,
        "source": "FUSED",
        "recordedAt": "2026-03-16T14:00:00Z"
      }
    ]
  }' -StatusCodeVariable status

$status
```

### US-021: Ver membros no mapa

```powershell
# Primeiro faça login para obter o token (veja US-002 acima)
# Obter localização de todos os membros do círculo (retorna 200 OK)
$locations = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/REPLACE_WITH_CIRCLE_ID/members/locations" `
  -Method GET `
  -Headers @{ Authorization = "Bearer $accessToken" }

$locations | ConvertTo-Json -Depth 5
```

### US-023: Visualizar status de compartilhamento no mapa

O endpoint `GET /api/v1/circles/{circleId}/members/locations` agora retorna o campo `sharingStatus` com valores: `ONLINE`, `STALE` ou `PAUSED`.

- **ONLINE**: localização recente (menos de 5 minutos)
- **STALE**: localização desatualizada (mais de 5 minutos)
- **PAUSED**: compartilhamento pausado (sem dados de localização)

```powershell
# Obter membros com status de compartilhamento
$locations = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/REPLACE_WITH_CIRCLE_ID/members/locations" `
  -Method GET `
  -Headers @{ Authorization = "Bearer $accessToken" }

# Exemplo de resposta:
# [
#   { "userId": "...", "fullName": "John", "sharingStatus": "ONLINE", "latitude": -23.56, ... },
#   { "userId": "...", "fullName": "Jane", "sharingStatus": "STALE", "latitude": -23.57, ... },
#   { "userId": "...", "fullName": "Bob",  "sharingStatus": "PAUSED", "latitude": null, ... }
# ]

$locations | ConvertTo-Json -Depth 5
```

```powershell
# Filtrar apenas membros online
$online = $locations | Where-Object { $_.sharingStatus -eq "ONLINE" }
$online | ConvertTo-Json -Depth 5
```

```powershell
# Filtrar membros com localização desatualizada
$stale = $locations | Where-Object { $_.sharingStatus -eq "STALE" }
$stale | ConvertTo-Json -Depth 5
```

```powershell
# Filtrar membros com compartilhamento pausado
$paused = $locations | Where-Object { $_.sharingStatus -eq "PAUSED" }
$paused | ConvertTo-Json -Depth 5
```

### US-022: Pausar compartilhamento de localização

```powershell
# Primeiro faça login para obter o token (veja US-002 acima)
# Pausar compartilhamento com prazo definido (retorna 202 Accepted)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/REPLACE_WITH_CIRCLE_ID/location-sharing/pause" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{
    "pausedUntil": "2026-03-16T23:59:59Z"
  }' -StatusCodeVariable status

$status
```

```powershell
# Pausar compartilhamento por tempo indeterminado (retorna 202 Accepted)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/REPLACE_WITH_CIRCLE_ID/location-sharing/pause" `
  -Method POST -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '{}' -StatusCodeVariable status

$status
```

```powershell
# Retomar compartilhamento (retorna 202 Accepted)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/circles/REPLACE_WITH_CIRCLE_ID/location-sharing/resume" `
  -Method POST `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -StatusCodeVariable status

$status
```

### US-020/021/022/023: rodar testes automatizados do pacote

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=LocationTest,LocationSharingStateTest,StreamLocationServiceTest,GetCircleMembersLocationServiceTest,PauseLocationSharingServiceTest,ResumeLocationSharingServiceTest,LocationControllerTest" `
  test "-Dsurefire.useFile=false"
```

### US-132: Listar dispositivos do usuário

```powershell
# Primeiro faça login para obter o token (veja US-002 acima)
$devices = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/me/devices" `
  -Method GET `
  -Headers @{ Authorization = "Bearer $accessToken" }

$devices | ConvertTo-Json -Depth 5
```

### US-132: Revogar sessão de dispositivo específico

```powershell
# Usa o deviceId retornado pelo endpoint de listar dispositivos acima
$deviceId = $devices[0].id
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/me/devices/$deviceId" `
  -Method DELETE `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -StatusCodeVariable status
$status
```

### US-132: rodar testes automatizados do pacote

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=ListUserDevicesServiceTest,RevokeDeviceServiceTest,UserControllerTest" `
  test "-Dsurefire.useFile=false"
```

## 8. Swagger UI

```
http://localhost:8080/swagger-ui.html
```

## 9. Kafka UI

```
http://localhost:8090
```

## 10. Observabilidade — Grafana LGTM

A stack LGTM (Loki + Grafana + Tempo + Prometheus) é provida pelo container `grafana/otel-lgtm`.

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| Grafana | <http://localhost:3000> | admin / admin |
| Prometheus | <http://localhost:9090> | — |
| OTLP gRPC | localhost:4317 | — |
| OTLP HTTP | localhost:4318 | — |

### Acessar dashboards no Grafana

1. Abra <http://localhost:3000> (login: `admin` / `admin`)
2. Vá em **Explore** no menu lateral
3. Selecione o datasource:
   - **Loki** — para visualizar logs da aplicação
   - **Tempo** — para visualizar traces distribuídos
   - **Prometheus** — para visualizar métricas

### Consultar logs no Loki (Explore)

```logql
{application="family-locator"}
{application="family-locator"} |= "error"
{application="family-locator", level="ERROR"}
```

### Consultar traces no Tempo (Explore)

Use a busca por **Service Name** = `family-locator` ou cole um `traceId` dos logs.

### Reiniciar apenas o container de observabilidade

```powershell
docker compose -f docker-compose.infra.yml restart otel-lgtm
```

## 11. Stack completa (infra + app containerizada)

```powershell
docker compose up -d --build
```

---

## Referência: Comandos de Infraestrutura

### Parar todos os containers

```powershell
docker compose -f docker-compose.infra.yml down
```

### Parar e remover volumes (reset completo)

```powershell
docker compose -f docker-compose.infra.yml down -v
```

### Testar conectividade com o Docker Hub

```powershell
Test-NetConnection registry-1.docker.io -Port 443 -WarningAction SilentlyContinue | Select-Object ComputerName, RemotePort, TcpTestSucceeded
```

## Verificar os tópicos do kafka

```
docker exec fl-kafka kafka-topics --list --bootstrap-server localhost:9092

drive.events
geofence.events
location.events
notification.commands
sos.events
```

## Criar um tópico no kafka

```
docker exec fl-kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic location.events --partitions 6 --replication-factor 1 --config retention.ms=604800000 --config cleanup.policy=delete

docker exec fl-kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic geofence.events --partitions 3 --replication-factor 1 --config retention.ms=604800000

docker exec fl-kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic drive.events --partitions 3 --replication-factor 1 --config retention.ms=604800000

docker exec fl-kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic sos.events --partitions 3 --replication-factor 1 --config retention.ms=604800000

docker exec fl-kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic notification.commands --partitions 3 --replication-factor 1 --config retention.ms=604800000
```

## Verificar as extensões do PostgreSQL

```
docker exec fl-postgres psql -U fl_user -d family_locator -c "SELECT extname, extversion FROM pg_extension ORDER BY extname;"

  extname  | extversion 
-----------+------------
 pg_trgm   | 1.6
 plpgsql   | 1.0
 postgis   | 3.4.3
 uuid-ossp | 1.1
```

## Perfil Solo: proteção da `main` (PR + CI, sem aprovador externo)

> Use quando você estiver trabalhando sozinho no repositório e ainda quiser manter PR obrigatório com check de CI.

```powershell
$payload = @'
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["build-and-test"]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 0,
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "require_last_push_approval": false
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_linear_history": true,
  "block_creations": false,
  "required_conversation_resolution": false,
  "lock_branch": false,
  "allow_fork_syncing": false
}
'@

$tmp = New-TemporaryFile
$payload | Out-File -FilePath $tmp -Encoding ascii
gh api --method PUT repos/romanzini/locator360/branches/main/protection --input $tmp
Remove-Item $tmp -Force
```

### Validar configuração aplicada

```powershell
gh api repos/romanzini/locator360/branches/main/protection --jq "{enforce_admins: .enforce_admins.enabled, strict: .required_status_checks.strict, contexts: .required_status_checks.contexts, approvals: .required_pull_request_reviews.required_approving_review_count, conversation_resolution: .required_conversation_resolution.enabled, force_pushes: .allow_force_pushes.enabled, deletions: .allow_deletions.enabled}"
```
