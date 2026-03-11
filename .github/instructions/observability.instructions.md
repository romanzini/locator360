---
applyTo: '**'
---

# Observabilidade — Regras Obrigatórias

> Todo código de produção **DEVE** seguir estas diretrizes de observabilidade. Código sem logging e métricas adequados **NÃO** será aceito.

## 📐 Pilares da Observabilidade

| Pilar | Tecnologia | Propósito |
|-------|-----------|-----------|
| **Logging** | SLF4J + Logback + Loki | Registro estruturado de eventos (console + Loki) |
| **Metrics** | Micrometer + Prometheus + OTLP | Métricas de negócio e técnicas |
| **Tracing** | Micrometer Tracing + OpenTelemetry + Tempo | Rastreamento distribuído entre serviços |
| **Health** | Spring Actuator | Verificação de saúde dos componentes |
| **Visualização** | Grafana (via `grafana/otel-lgtm`) | Dashboards, exploração de logs/traces/métricas |

## 📝 Logging — Regras Obrigatórias

### Anotação `@Slf4j`
- **Obrigatório** em todas as classes de produção: Services, Controllers, Repositories, Consumers, Publishers, Adapters
- **Nunca** crie loggers manualmente (`LoggerFactory.getLogger(...)`) — use Lombok `@Slf4j`

### Níveis de Log — Quando Usar

| Nível | Uso | Exemplo |
|-------|-----|---------|
| `ERROR` | Falha que impede a operação de completar | `log.error("Failed to save user: {}", userId, exception)` |
| `WARN` | Situação inesperada mas recuperável | `log.warn("Retry attempt {} for user: {}", attempt, userId)` |
| `INFO` | Evento de negócio relevante (início/fim de operação) | `log.info("User registered successfully: {}", userId)` |
| `DEBUG` | Detalhes técnicos para troubleshooting | `log.debug("Finding user by email: {}", email)` |

### Padrões de Logging por Camada

#### Controllers (API)
```java
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController implements CustomerControllerApi {

    @Override
    public ResponseEntity<CustomerOutputDto> create(@Valid @RequestBody CreateCustomerInputDto input) {
        log.debug("Received create customer request: {}", input.getEmail());
        CustomerOutputDto output = createCustomerUseCase.execute(input);
        log.info("Customer created successfully: {}", output.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }
}
```

#### Application Services
```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreateCustomerService implements CreateCustomerUseCase {

    @Override
    public CustomerOutputDto execute(CreateCustomerInputDto input) {
        log.debug("Creating customer with email: {}", input.getEmail());
        
        // ... business logic ...
        
        log.info("Customer created: {}", savedCustomer.getId());
        return modelMapper.map(savedCustomer, CustomerOutputDto.class);
    }
}
```

#### Infrastructure (Repositories, Clients, Publishers)
```java
@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomerJpaRepository implements CustomerRepository {

    @Override
    public Customer save(Customer customer) {
        log.debug("Persisting customer: {}", customer.getId());
        // ... persistence logic ...
        log.debug("Customer persisted successfully: {}", customer.getId());
        return result;
    }
}
```

#### Kafka Consumers
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class GeofenceConsumer {

    @KafkaListener(topics = "location.events")
    public void consume(LocationEvent event) {
        log.info("Received location event for user: {}", event.getUserId());
        // ... processing ...
        log.info("Location event processed for user: {}", event.getUserId());
    }
}
```

### ⚠️ Regras de Segurança em Logs
- **NUNCA** logar: senhas, tokens JWT, hashes de senha, dados de cartão
- **MASCARAR** dados sensíveis: email parcial (`j***@email.com`), telefone parcial (`***1234`)
- **PERMITIDO** logar: UUIDs, nomes de operação, status codes, tempos de execução
- Use `{}` para interpolação (SLF4J) — **NUNCA** concatenação de strings

```java
// ✅ CORRETO
log.info("User registered: {}", userId);

// ❌ ERRADO - concatenação
log.info("User registered: " + userId);

// ❌ ERRADO - dado sensível
log.info("User password: {}", password);
```

### Logging em Exceções
- **Sempre** inclua a exceção como último parâmetro do `log.error/warn`
- Logue no ponto de tratamento, **não** na propagação

```java
// ✅ CORRETO - log no handler
try {
    customerRepository.save(customer);
} catch (DataIntegrityViolationException ex) {
    log.error("Failed to save customer: {}", customer.getId(), ex);
    throw new CustomerAlreadyExistsException(customer.getEmail(), ex);
}

// ❌ ERRADO - log + rethrow sem tratamento
catch (Exception ex) {
    log.error("Error", ex);
    throw ex; // Log redundante se o handler acima já loga
}
```

## 📊 Métricas — Regras Obrigatórias

### Dependências Requeridas
- `spring-boot-starter-actuator` — endpoints de health/metrics
- `micrometer-registry-prometheus` — exportação para Prometheus

### Métricas Automáticas (Spring Boot Actuator)
O Actuator já fornece automaticamente:
- **HTTP Metrics**: `http.server.requests` (tempo, status, URI)
- **JVM Metrics**: memória, GC, threads
- **Database Metrics**: pool de conexões, queries
- **Kafka Metrics**: consumer lag, throughput

### Métricas de Negócio Customizadas
Para operações de negócio críticas, crie métricas customizadas usando `MeterRegistry`:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserService implements RegisterUserUseCase {

    private final MeterRegistry meterRegistry;

    @Override
    public RegisterUserOutputDto registerWithEmail(RegisterWithEmailInputDto input) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // ... business logic ...
            
            meterRegistry.counter("users.registered", "method", "email").increment();
            log.info("User registered with email: {}", savedUser.getId());
            return result;
        } catch (Exception ex) {
            meterRegistry.counter("users.registration.failed", "method", "email").increment();
            throw ex;
        } finally {
            sample.stop(meterRegistry.timer("users.registration.duration", "method", "email"));
        }
    }
}
```

### Naming Conventions para Métricas
- **Formato**: `{domínio}.{ação}` — ex: `users.registered`, `circles.created`
- **Tags**: Use tags para dimensões — ex: `"method", "email"`, `"status", "success"`
- **Timer**: Para operações com duração relevante
- **Counter**: Para contagem de eventos
- **Gauge**: Para valores atuais (ex: membros online)

### Métricas Prioritárias por Domínio

| Domínio | Métrica | Tipo | Tags |
|---------|---------|------|------|
| Auth | `users.registered` | Counter | method (email/phone) |
| Auth | `users.login` | Counter | method, status |
| Circle | `circles.created` | Counter | — |
| Circle | `circles.members.joined` | Counter | role |
| Location | `locations.ingested` | Counter | — |
| Location | `locations.ingestion.duration` | Timer | — |
| Geofence | `geofence.events` | Counter | type (enter/exit) |
| Drive | `drives.detected` | Counter | — |
| Drive | `safety.score.calculated` | Timer | — |
| SOS | `sos.events.triggered` | Counter | type (manual/auto) |
| Notification | `notifications.sent` | Counter | channel (push/sms/email) |
| Notification | `notifications.failed` | Counter | channel, reason |

## 🏥 Health Checks — Regras Obrigatórias

### Endpoints Atuator Expostos
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always
```

### Health Indicators Customizados (quando necessário)
Para dependências externas críticas, crie health indicators:

```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Health health() {
        try {
            // Verifica conexão com Kafka
            kafkaTemplate.getDefaultTopic();
            return Health.up().withDetail("kafka", "connected").build();
        } catch (Exception ex) {
            return Health.down().withDetail("kafka", "disconnected").withException(ex).build();
        }
    }
}
```

## 🔗 Tracing Distribuído

### Configuração
O Spring Boot 3 com Micrometer Tracing + OpenTelemetry propaga automaticamente `traceId` e `spanId` entre serviços via headers HTTP e Kafka.

**Dependências obrigatórias (já configuradas no `pom.xml`):**
- `micrometer-tracing-bridge-otel` — Bridge Micrometer → OpenTelemetry
- `opentelemetry-exporter-otlp` — Exporta traces via protocolo OTLP para Tempo

**Configuração OTLP (já configurada no `application.yml`):**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% em dev
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
    metrics:
      endpoint: http://localhost:4318/v1/metrics
```

### Logs Correlacionados
O `traceId` e `spanId` são automaticamente incluídos nos logs via MDC. O pattern é definido no `logback-spring.xml`:
```
%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [family-locator,traceId,spanId] [thread] logger : message
```

### Envio de Logs para Loki
O `logback-spring.xml` configura o appender `loki4j` (ativo apenas no profile `dev`) que envia logs diretamente para Loki com labels `{application, level, logger}`, permitindo correlação com traces no Grafana.

**Dependência:** `com.github.loki4j:loki-logback-appender`

**Configuração do host Loki (via `application-dev.yml`):**
```yaml
loki:
  host: ${LOKI_HOST:localhost}
  port: ${LOKI_PORT:3100}
```

### Span Customizado (quando necessário)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class GeofenceService {

    private final ObservationRegistry observationRegistry;

    public void evaluateGeofences(LocationEvent event) {
        Observation.createNotStarted("geofence.evaluation", observationRegistry)
            .lowCardinalityKeyValue("circleId", event.getCircleId().toString())
            .observe(() -> {
                // ... geofence evaluation logic ...
                log.info("Geofence evaluated for user: {}", event.getUserId());
            });
    }
}
```

## 📁 Estrutura para Observabilidade

```text
src/main/resources/
├── application.yml            # management.otlp.tracing/metrics endpoints
├── application-dev.yml        # loki.host, loki.port
└── logback-spring.xml         # Console + Loki appender (dev profile)

infrastructure/
└── config/
    └── ObservabilityConfig.java    # Beans de MeterRegistry, ObservationRegistry (se customizações necessárias)
```

Não é necessária uma pasta dedicada — as configurações do Actuator e Micrometer são gerenciadas via `application.yml`.

## 🐳 Stack LGTM — Ambiente de Desenvolvimento

O projeto utiliza a imagem `grafana/otel-lgtm` (all-in-one) que empacota:
- **Grafana** — Dashboards e exploração (porta 3000, login `admin`/`admin`)
- **Loki** — Agregação de logs (porta 3100)
- **Tempo** — Armazenamento de traces (recebe via OTLP)
- **Prometheus** — Scrape de métricas (porta 9090)
- **OpenTelemetry Collector** — Recebe traces/métricas via OTLP (portas 4317 gRPC, 4318 HTTP)

### Fluxo de dados
```
App (Spring Boot)
  ├─ Logs (loki4j appender) ───────→ Loki ──→ Grafana (Explore/Logs)
  ├─ Traces (OTLP HTTP) ──────────→ OTel Collector ──→ Tempo ──→ Grafana (Explore/Traces)
  └─ Metrics (/actuator/prometheus) → Prometheus scrape ──→ Grafana (Explore/Metrics)
```

### Iniciar a stack
```bash
docker compose -f docker-compose.infra.yml up -d
```

### Acessar o Grafana
Abra http://localhost:3000 → **Explore** → selecione Loki, Tempo ou Prometheus.

### Queries úteis no Loki
```logql
{application="family-locator"}                    # todos os logs
{application="family-locator", level="ERROR"}     # apenas erros
{application="family-locator"} |= "userId"        # busca por texto
```

## ✅ Checklist de Observabilidade

Antes de considerar qualquer funcionalidade completa, verifique:

- [ ] `@Slf4j` presente em todas as classes de produção
- [ ] `log.debug` na entrada dos métodos públicos
- [ ] `log.info` para eventos de negócio relevantes
- [ ] `log.error` com exceção como parâmetro em blocos catch
- [ ] Nenhum dado sensível nos logs
- [ ] Métricas de negócio para operações críticas (Counter/Timer)
- [ ] Health check funcional (`/actuator/health`)
- [ ] Métricas expostas (`/actuator/prometheus`)
