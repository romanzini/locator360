---
applyTo: '**'
---

# Locator 360 — Referência do Projeto

> Este arquivo contém o contexto específico do projeto Locator 360 para orientar a IA durante a implementação.
> Para regras arquiteturais genéricas, consulte `vexa-architecture.instructions.md` e as instructions por camada.

## Visão Geral

O **Locator 360** é uma plataforma de monitoramento familiar focada em segurança, rastreamento de localização em tempo real e análise de comportamento de direção. Famílias e grupos criam "Círculos" para compartilhar localização, receber alertas de geofencing e serem notificados em emergências.

## Tech Stack

| Categoria | Tecnologia |
|-----------|-----------|
| Linguagem | Java 17+ |
| Framework | Spring Boot 3+ (Web MVC, Security, Data JPA, Kafka, Validation) |
| Banco de Dados | PostgreSQL 15+ com PostGIS (dados geoespaciais) |
| Cache | Redis 7+ (última localização, sessões) |
| Event Streaming | Apache Kafka 3+ |
| ORM | Hibernate 6 |
| Migrações | Flyway |
| Autenticação | JWT (jjwt / nimbus-jose) + BCrypt |
| Documentação API | SpringDoc OpenAPI 3.0 (Swagger) |
| Mapeamento | ModelMapper |
| Utilitários | Lombok |
| Serialização | Jackson |
| Observabilidade | Spring Actuator, Micrometer, Logback/SLF4J |
| Testes | JUnit 5, Mockito, Testcontainers, MockMvc |
| Infraestrutura | Docker, Docker Compose, Kubernetes, Nginx/ALB |

## Funcionalidades Principais

1. **Localização em Tempo Real** — Rastreamento contínuo, otimização de bateria, atualização em background
2. **Gestão de Círculos** — Múltiplos grupos, convites via código/link, papéis ADMIN/MEMBER
3. **Lugares & Geofencing** — Cadastro de locais, detecção enter/exit, notificações personalizáveis
4. **Driving Safety** — Detecção automática de viagens, análise de risco (velocidade, frenagem, uso de celular), Safety Score 0-100
5. **SOS & Emergência** — Botão de pânico, broadcast para membros do círculo
6. **Chat & Check-in** — Chat de grupo por círculo, check-ins manuais com localização
7. **Notificações** — Push/SMS/email, preferências por tipo e círculo, mute temporário
8. **Billing & Plans** — Planos FREE/PREMIUM, integração Google Play e App Store
9. **Admin & Auditoria** — Backoffice com papéis SUPPORT/ADMIN/SUPER_ADMIN, flags, audit log

## Domínios de Negócio (10)

| Domínio | Entidades Principais | Regras-Chave |
|---------|---------------------|-------------|
| **Auth & Account** | User, AuthIdentity, Device, VerificationToken | Multi-provider login, verificação, sessões por dispositivo |
| **Circles & Members** | Circle, CircleMember, CircleInvite, CircleSettings | Papéis ADMIN/MEMBER, convites com expiração, transferência de admin |
| **Location & History** | Location, LocationSharingState | Ingestão em lote, última posição, pausa de compartilhamento |
| **Places & Geofence** | Place, PlaceAlertPolicy, PlaceAlertTarget, PlaceEvent | Detecção enter/exit, políticas por dia/horário, destinatários custom |
| **Driving & Safety** | Drive, DriveEvent, SafetyScore | Detecção automática de viagens (CAR), eventos de risco, score 0-100 |
| **SOS & Incidents** | SosEvent, IncidentDetection | SOS manual/automático, detecção de colisão, workflow OPEN→RESOLVED |
| **Chat & Checkin** | CircleMessage, CircleMessageReceipt, Checkin | Chat por círculo, recibos de leitura, check-in com localização |
| **Notifications** | Notification, NotificationPreference | Push/SMS/email, preferências por tipo e por círculo, mute temporário |
| **Billing & Plans** | Plan, Subscription | Planos FREE/PREMIUM, integração com Google Play e App Store |
| **Admin & Audit** | AdminUser, UserFlag, AuditLog | Papéis SUPPORT/ADMIN/SUPER_ADMIN, flags de abuso, trilha de auditoria |

## REST Controllers (11)

| Controller | Endpoints | Responsabilidade |
|-----------|-----------|------------------|
| Auth | `/auth/*` | Registro, login, verificação, tokens JWT |
| User | `/users/*` | CRUD de perfil do usuário autenticado |
| Circles | `/circles/*` | Criação, gestão de membros, convites, configurações |
| Location | `/locations/*` | Ingestão em lote (stream) de eventos GPS → publica no Kafka |
| Places | `/circles/{circleId}/places/*` | CRUD de lugares, políticas de alerta, geofencing |
| Driving | `/drives/*` | Consulta de viagens, eventos de direção, safety score |
| SOS | `/sos/*` | Acionamento e resolução de emergências → publica no Kafka |
| Chat | `/circles/{circleId}/messages/*` | Chat de grupo e check-ins manuais |
| Notifications | `/notifications/*` | Preferências e histórico de notificações |
| Plans | `/plans/*`, `/subscriptions/*` | Planos disponíveis e gestão de assinaturas |
| Admin | `/admin/*` | Backoffice: busca de usuários, flags, auditoria |

## Kafka Consumers (4)

| Consumer | Tópico(s) Consumido(s) | Responsabilidade |
|----------|----------------------|------------------|
| Geofence Consumer | `location.events` | Avalia localização contra geofences → gera `geofence.events` |
| Drive Detection Consumer | `location.events` | Algoritmo stateful de detecção de viagens → gera `drive.events` |
| Notification Dispatch Consumer | `geofence.events`, `drive.events`, `notification.commands` | Despacha push/SMS/email |
| SOS Broadcast Consumer | `sos.events` | Distribui alertas de emergência para membros do círculo |

## Tópicos Kafka (5)

| Tópico | Particionamento | Produtor | Consumidor(es) |
|--------|----------------|----------|----------------|
| `location.events` | `userId` | Location Service | Geofence Consumer, Drive Detection Consumer |
| `geofence.events` | `circleId` | Geofence Consumer | Notification Dispatch Consumer |
| `drive.events` | `userId` | Drive Detection Consumer | Notification Dispatch Consumer |
| `notification.commands` | `userId` | Qualquer Service | Notification Dispatch Consumer |
| `sos.events` | `circleId` | SOS Service | SOS Broadcast Consumer |

## Adaptadores de Saída (Infrastructure) (8)

| Adaptador | Serviço Externo | Função |
|-----------|----------------|--------|
| JPA/Hibernate Repositories | PostgreSQL + Redis | Persistência relacional e cache de última localização |
| Kafka Event Publisher | Apache Kafka | Publicação de eventos de domínio nos tópicos |
| Geofence Engine | PostgreSQL (PostGIS) | Cálculo de entrada/saída em cercas virtuais |
| Drive Detection Engine | PostgreSQL | Detecção de viagens baseada em velocidade/tempo |
| Push Notification Adapter | FCM / APNS | Notificações push para Android e iOS |
| SMS/Email Adapter | SMS Gateway / Email Provider | Verificação de conta, alertas SOS |
| Maps/Geocoding Adapter | Google Maps API | Geocodificação reversa (coordenadas → endereço) |
| Store Billing Adapter | Google Play / App Store | Validação de compras in-app e assinaturas |

## Estrutura de Pastas do Backend

```text
/src/main/java/com/locator360
├── api/                              # Adaptadores de Entrada
│   ├── rest/                         # Controllers REST (síncrono)
│   │   ├── auth/
│   │   ├── user/
│   │   ├── circle/
│   │   ├── location/
│   │   ├── place/
│   │   ├── drive/
│   │   ├── sos/
│   │   ├── chat/
│   │   ├── notification/
│   │   ├── plan/
│   │   ├── admin/
│   │   └── config/                   # Configurações REST (CORS, interceptors, etc.)
│   └── kafka/                        # Kafka Consumers (assíncrono)
│       ├── geofence/
│       ├── drive/
│       ├── notification/
│       └── sos/
├── core/                             # Núcleo da Aplicação
│   ├── domain/                       # Entidades, Value Objects, Enums
│   │   ├── user/
│   │   ├── circle/
│   │   ├── location/
│   │   ├── place/
│   │   ├── drive/
│   │   ├── sos/
│   │   ├── chat/
│   │   ├── notification/
│   │   ├── plan/
│   │   ├── admin/
│   │   ├── vo/                       # Value Objects compartilhados
│   │   └── service/                  # Domain Services
│   ├── application/                  # Casos de Uso
│   │   ├── service/                  # Application Services (implementam Ports IN)
│   │   │   ├── auth/
│   │   │   ├── circle/
│   │   │   ├── location/
│   │   │   ├── place/
│   │   │   ├── drive/
│   │   │   ├── sos/
│   │   │   ├── chat/
│   │   │   ├── notification/
│   │   │   ├── plan/
│   │   │   └── admin/
│   │   └── mapper/                   # (Opcional) Configurações complexas de ModelMapper
│   └── port/                         # Interfaces
│       ├── in/                       # Ports IN (Use Cases interfaces)
│       │   ├── auth/
│       │   ├── circle/
│       │   ├── location/
│       │   ├── place/
│       │   ├── drive/
│       │   ├── sos/
│       │   ├── chat/
│       │   ├── notification/
│       │   ├── plan/
│       │   ├── admin/
│       │   └── dto/
│       │       ├── input/
│       │       └── output/
│       └── out/                      # Ports OUT (Repository/Integration interfaces)
└── infrastructure/                   # Adaptadores de Saída
    ├── persistence/                  # Persistência (JPA/Spring Data)
    │   └── postgresql/
    │       ├── entity/               # JPA Entities (por domínio)
    │       ├── repository/           # Implementação dos Ports OUT de persistência
    │       ├── mapper/               # (Opcional) Configurações complexas Entity ↔ Domain
    │       └── config/               # Configurações de banco de dados
    ├── rest/                         # Clientes REST para APIs externas
    │   ├── notification/             # FCM/APNS adapter
    │   ├── sms/                      # SMS gateway adapter
    │   ├── maps/                     # Geocoding adapter
    │   ├── billing/                  # Store billing adapter
    │   ├── mapper/                   # (Opcional) Configurações complexas
    │   ├── config/                   # Configurações de RestTemplate/WebClient
    │   └── properties/              # Properties dos clientes externos
    └── event/                        # Event Publishers
        └── kafka/
            ├── publisher/            # Publicação nos tópicos
            ├── mapper/               # (Opcional) Configurações complexas
            ├── config/               # Configurações Kafka
            └── properties/           # Properties de eventos
```

## Fluxo Event-Driven Principal

```
Mobile App → API Gateway → REST Controller (POST /locations/stream)
  → Location Service (Core) → Port OUT → Kafka Publisher → location.events
  → 202 Accepted (resposta imediata)

Processamento assíncrono em paralelo:
  location.events → Geofence Consumer → geofence.events → Notification Consumer → Push/SMS/Email
  location.events → Drive Detection Consumer → drive.events → Notification Consumer → Push/SMS/Email
```

## Documentação de Referência (pasta `Docs/`)

Consulte estes arquivos para decisões de implementação detalhadas:

| Arquivo | Conteúdo | Quando consultar |
|---------|----------|-----------------|
| `Docs/especificacao-funcional.md` | Requisitos de negócio, personas, regras por módulo | Implementar qualquer caso de uso ou validação de negócio |
| `Docs/database-model.md` | Tabelas, colunas, tipos, relacionamentos, constraints | Criar JPA Entities, migrations Flyway, queries |
| `Docs/openapi.yaml` | Especificação OpenAPI 3.0 dos endpoints | Implementar controllers REST, DTOs de entrada/saída |
| `Docs/detection-strategy.md` | Algoritmos de detecção de viagem e eventos de risco | Implementar Drive Detection Consumer e SafetyScore |
| `Docs/detection-pseudocode.md` | Pseudocódigo dos algoritmos de detecção | Implementar lógica de detecção no código |
| `Docs/location-tracking-strategy.md` | Estratégia de coleta GPS, otimização de bateria | Implementar Location Service e ingestão de eventos |
| `Docs/api-usage.md` | Guia de uso da API com exemplos de requests/responses | Validar contratos de entrada/saída dos endpoints |
| `Docs/api-consistency-audit.md` | Auditoria de consistência dos endpoints | Verificar padrões REST e nomenclatura |
| `Docs/backlog.md` | Backlog de funcionalidades e tarefas pendentes | Priorizar implementações e entender escopo |
| `Docs/entity-endpoint-postman-matrix.md` | Matriz entidade × endpoint | Mapear quais entidades cada endpoint consome/produz |
| `Docs/diagrams.md` | Diagramas de arquitetura e fluxos | Entender fluxos visuais entre componentes |
| `Docs/postman-collection.json` | Collection Postman com requests prontos | Referência de payloads e headers esperados |

## Observabilidade

O projeto segue regras obrigatórias de observabilidade. Consulte `.github/instructions/observability.instructions.md` para:
- Padrões de logging por camada (Controllers, Services, Repositories, Consumers)
- Métricas de negócio customizadas com Micrometer
- Health checks e endpoints Actuator
- Regras de segurança em logs (dados sensíveis)
- Checklist de observabilidade por funcionalidade

## Testes e Execução Local

O projeto segue regras obrigatórias para testes, compilação e execução local via Docker. Consulte `.github/instructions/testing.instructions.md` para:
- Comandos oficiais de teste e compilação com Docker Maven
- Regra de não utilizar `mvn` local para `test`, `compile` ou `spring-boot:run`
- Referência obrigatória ao `commands.md` como fonte de verdade

## Git Commits & Small Releases

O projeto adota **small releases** com **Conventional Commits**. Consulte `.github/instructions/git-commits.instructions.md` para:
- Formato obrigatório de mensagens de commit
- Granularidade correta: um commit por camada/responsabilidade
- Scopes por domínio de negócio
- Workflow de commit integrado ao TDD
- Checklist de commit
