# Family Locator — Comandos de Desenvolvimento

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

## 6. Testar os endpoints

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

## 7. Swagger UI

```
http://localhost:8080/swagger-ui.html
```

## 8. Kafka UI

```
http://localhost:8090
```

## 9. Stack completa (infra + app containerizada)

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
