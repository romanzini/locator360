# Locator 360 — Comandos de Desenvolvimento

## 1. Subir a infraestrutura (PostgreSQL, Redis, Kafka, Zookeeper)

```powershell
docker compose -f docker-compose.infra.yml up -d
```

Aguarde todos os containers ficarem _Healthy_ antes de prosseguir.

## 2. Verificar containers em execução

```powershell
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String "fl-"
```

Resultado esperado (todos healthy):

| Container | Porta |
|-----------|-------|
| fl-postgres | 5432 |
| fl-redis | 6379 |
| fl-kafka | 9092 |
| fl-zookeeper | 2181 |
| fl-kafka-ui | 8090 |

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
  maven:3.9.9-eclipse-temurin-17 mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
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
  --title "<type>(<scope>): <description>" `
  --body "<descrição do PR>"
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

### Registro com email

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register/email" `
  -Method POST -ContentType "application/json" `
  -Body '{"email":"maria@example.com","password":"SenhaForte123!","fullName":"Maria Oliveira"}'
```

### Registro com telefone

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register/phone" `
  -Method POST -ContentType "application/json" `
  -Body '{"phoneNumber":"+5511999999999","verificationCode":"123456","fullName":"Ana Santos"}'
```

### US-002: login com email

```powershell
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login/email" `
  -Method POST -ContentType "application/json" `
  -Body '{"email":"maria@example.com","password":"SenhaForte123!"}'

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

### Observação sobre `/api/v1/users/me`

```text
- Sem header Authorization: Bearer <token>, a resposta esperada é 401 Unauthorized.
- No estado atual do projeto, `/api/v1/users/me` ainda não está implementado no backend.
- Esse endpoint faz parte do US-004 (Atualizar perfil), que ainda está pendente.
```

### Exemplo futuro para endpoint autenticado

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/me" `
  -Method GET `
  -Headers @{ Authorization = "Bearer $accessToken" }
```

## 8. Swagger UI

```
http://localhost:8080/swagger-ui.html
```

## 9. Kafka UI

```
http://localhost:8090
```

## 10. Stack completa (infra + app containerizada)

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
