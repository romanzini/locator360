# Diagramas

## 1. Diagrama de Contexto (Nível 0 – Alto Nível)

**Atores externos:**

- Usuário (móvel)
- Serviços de Notificação (FCM/APNS, SMS gateway, e-mail provider)
- Lojas de Aplicativos (Google Play, App Store)
- Serviços de Mapas/GPS (Google Maps, Apple Maps, etc.)

**Sistema central:**

- Sistema FamilySafe 360 (Backend + Banco de Dados + Serviços de Localização)

Representação em Mermaid (pode ser colada em .md compatível):

```mermaid
graph LR
  User[User (Mobile App)] --> Backend[FamilySafe 360 Backend]
  Backend --> DB[Database]
  Backend --> Notif[Notification Services]
  Backend --> Maps[Maps and GPS Services]
  User --> Stores[App Stores]
```

---

## 2. Diagrama de Casos de Uso – Usuário Final (Resumo)

Principais casos de uso:

- UC-01: Cadastrar-se e autenticar-se
- UC-02: Criar e gerenciar círculos
- UC-03: Compartilhar localização em tempo real
- UC-04: Ver membros no mapa
- UC-05: Cadastrar lugares e receber alertas
- UC-06: Consultar histórico de localização
- UC-07: Monitorar e ver relatórios de direção
- UC-08: Acionar e responder a SOS
- UC-09: Enviar mensagens no chat do círculo
- UC-10: Gerenciar notificações e preferências
- UC-11: Gerenciar plano e assinatura
- UC-12: Configurar privacidade

Esboço Mermaid (não é UML puro, mas ajuda na visualização):

```mermaid
graph LR
  actorUser[User]

  subgraph System[FamilySafe 360]
    UC1[Sign up / Login]
    UC2[Manage Circles]
    UC3[Share Location]
    UC4[View Members on Map]
    UC5[Manage Places / Geofences]
    UC6[View History]
    UC7[Driving Reports]
    UC8[Send SOS]
    UC9[Circle Chat]
    UC10[Notification Settings]
    UC11[Plans and Subscription]
    UC12[Privacy Settings]
  end

  actorUser --> UC1
  actorUser --> UC2
  actorUser --> UC3
  actorUser --> UC4
  actorUser --> UC5
  actorUser --> UC6
  actorUser --> UC7
  actorUser --> UC8
  actorUser --> UC9
  actorUser --> UC10
  actorUser --> UC11
  actorUser --> UC12
```

---

## 3. Diagrama de Componentes Lógico (Alto Nível)

Componentes internos sugeridos:

- **App Mobile**
  - Módulo de Autenticação
  - Módulo de Mapas & Localização
  - Módulo de Círculos
  - Módulo de Lugares & Geofences
  - Módulo de Direção & SOS
  - Módulo de Chat
  - Módulo de Configurações
- **Backend**
  - Serviço de Autenticação & Contas
  - Serviço de Círculos e Membros
  - Serviço de Localização & Histórico
  - Serviço de Direção & Relatórios
  - Serviço de Notificações
  - Serviço de Billing & Planos
  - Painel Administrativo

Mermaid simplificado:

```mermaid
graph TB
  subgraph Mobile[App Mobile]
    MAuth[Auth and Account]
    MMap[Maps and Location]
    MCirc[Circles]
    MPlace[Places and Geofences]
    MDrive[Driving and SOS]
    MChat[Chat]
    MConfig[Settings]
  end

  subgraph Backend[Backend]
    BAuth[Auth Service]
    BCirc[Circles Service]
    BLoc[Location and History Service]
    BDrive[Driving Service]
    BNotif[Notification Service]
    BBill[Billing and Plans Service]
    BAdmin[Admin Panel]
  end

  Mobile --> Backend
  BNotif --> ExtNotif[FCM/APNS/SMS/E-mail]
  BLoc --> DBLoc[(BD Localização)]
  BCirc --> DBCirc[(BD Círculos)]
  BBill --> Stores[Lojas de Apps]
```

---

## 4. Diagrama de Estado – Localização de um Usuário (Opcional)

Estados típicos:

- DESATIVADA
- ATIVA_COMPARTILHANDO
- PAUSADA
- OFFLINE (sem rede, com buffer de eventos)

Mermaid:

```mermaid
graph LR
  DESATIVADA[Location Sharing Off]
  ATIVA_COMPARTILHANDO[Sharing Active]
  PAUSADA[Sharing Paused]
  OFFLINE[Offline]

  DESATIVADA --> ATIVA_COMPARTILHANDO
  ATIVA_COMPARTILHANDO --> PAUSADA
  PAUSADA --> ATIVA_COMPARTILHANDO
  ATIVA_COMPARTILHANDO --> OFFLINE
  OFFLINE --> ATIVA_COMPARTILHANDO
  ATIVA_COMPARTILHANDO --> DESATIVADA
```

```mermaid
sequenceDiagram
    participant U as Usuário
    participant A as App Frontend
    participant B as Backend API

    %% 1. Abrir app e verificar sessão
    U->>A: Abre o app
    A->>A: Verifica accessToken salvo
    alt Sem token
        A->>U: Exibe tela de Login/Registro
        U->>A: Envia email/senha ou telefone/código
        A->>B: POST /auth/login ou /auth/register
        B-->>A: 200 AuthResponse (accessToken, user)
        A->>A: Salva accessToken localmente
    else Com token
        A->>B: GET /users/me (valida token)
        B-->>A: 200 User (ou 401 se inválido)
        alt Token inválido
            A->>U: Volta para tela de Login
        end
    end

    %% 2. Carregar círculos e membros
    A->>B: GET /circles
    B-->>A: 200 [Circle]
    A->>U: Exibe lista de círculos / escolhe padrão
    U->>A: Seleciona círculo (circleId)
    A->>B: GET /circles/{circleId}/members
    B-->>A: 200 [CircleMember]

    %% 3. Buscar última localização de cada membro
    loop Para cada membro do círculo
        A->>B: GET /circles/{circleId}/members/{memberId}/location
        B-->>A: 200 Location (latitude, longitude, recordedAt)
        A->>A: Atualiza avatar do membro no mapa
    end
    A->>U: Mostra mapa com todos os membros

    %% 4. Envio periódico da localização do próprio usuário
    loop A cada intervalo de tempo
        A->>A: Coleta pontos de GPS
        A->>B: POST /locations/stream (events[])
        B-->>A: 202 Accepted
    end

    %% 5. Atualização periódica do mapa
    loop Atualização do mapa
        A->>B: GET /circles/{circleId}/members/{memberId}/location
        B-->>A: 200 Location (atualizada)
        A->>A: Move pins no mapa
    end

    %% 6. Ações a partir do mapa (exemplos)
    U->>A: Abre histórico de um membro
    A->>B: GET /circles/{circleId}/members/{memberId}/locations/history?start&end
    B-->>A: 200 [Location]
    A->>U: Mostra trajeto/histórico

    U->>A: Abre chat do círculo
    A->>B: GET /circles/{circleId}/messages
    B-->>A: 200 [CircleMessage]
    A->>U: Mostra lista de mensagens

    U->>A: Pressiona botão SOS
    A->>B: POST /sos (circleId, location opcional)
    B-->>A: 201 SosEvent
    A->>U: Confirmação SOS enviado
```

```mermaid
sequenceDiagram
    participant U as Usuário (dono do celular)
    participant A as App Frontend
    participant B as Backend API
    participant DB as Banco de Dados
    participant OM as App de outro membro

    %% 1. Envio de localização (dono do celular)
    loop Periodicamente
        A->>A: Coleta ponto de GPS (lat, lng, horário)
        A->>B: POST /locations/stream (events[])
        B->>DB: Grava eventos em locations (histórico)
        B->>DB: Atualiza última localização do usuário
        B-->>A: 202 Accepted
    end

    %% 2. Outro membro consulta a última posição
    OM->>B: GET /circles/{circleId}/members/{memberId}/location
    B->>DB: Busca última localização do memberId
    DB-->>B: Location (lat, lng, recordedAt)
    B-->>OM: 200 Location
    OM->>OM: Mostra posição no mapa
```

---

## 5. Diagrama de Componentes – Frontend ↔ Backend

Visão de alto nível de como o(s) frontend(s) se conectam ao backend e como este se organiza internamente:

```mermaid
graph LR
  subgraph Frontend
    Mobile[App Mobile]
    Web["App Web (opcional)"]
  end

  subgraph Backend["FamilySafe 360 Backend API"]
    APIGW[API REST /auth, /circles, /locations, ...]
    SAuth["Serviço de Autenticação & Contas"]
    SCircle["Serviço de Círculos & Membros"]
    SLoc["Serviço de Localização & Histórico"]
    SDrive["Serviço de Direção"]
    SSOS["Serviço de SOS"]
    SChat["Serviço de Chat"]
    SNotif["Serviço de Notificações"]
  end

  DB["Banco de Dados Relacional"]
  LocStore["Armazenamento de Localizações"]
  Push["Serviços de Push (FCM/APNS)"]
  Maps["Serviços de Mapas/GPS"]

  Mobile -->|HTTPS JSON + JWT| APIGW
  Web -->|HTTPS JSON + JWT| APIGW

  APIGW --> SAuth
  APIGW --> SCircle
  APIGW --> SLoc
  APIGW --> SDrive
  APIGW --> SSOS
  APIGW --> SChat
  APIGW --> SNotif

  SAuth --> DB
  SCircle --> DB
  SChat --> DB
  SDrive --> DB
  SSOS --> DB
  SLoc --> LocStore

  SNotif --> Push
  SLoc --> Maps
```

---

## 6. Diagrama ER Lógico de Referência (Rastreabilidade)

Este diagrama complementa os diagramas de fluxo/arquitetura e reflete as entidades de `database-model.md`.

> Nota: no domínio de direção, a nomenclatura canônica adotada é `drive`.

```mermaid
erDiagram
  USERS ||--o{ AUTH_IDENTITIES : has
  USERS ||--o{ VERIFICATION_TOKENS : verifies
  USERS ||--o{ DEVICES : owns

  USERS ||--o{ CIRCLE_MEMBERS : joins
  CIRCLES ||--o{ CIRCLE_MEMBERS : has
  CIRCLES ||--o{ CIRCLE_INVITES : has
  CIRCLES ||--|| CIRCLE_SETTINGS : configures

  USERS ||--o{ LOCATIONS : generates
  CIRCLES ||--o{ LOCATIONS : context
  USERS ||--o{ LOCATION_SHARING_STATES : controls
  CIRCLES ||--o{ LOCATION_SHARING_STATES : controls

  CIRCLES ||--o{ PLACES : has
  PLACES ||--o{ PLACE_ALERT_POLICIES : defines
  PLACE_ALERT_POLICIES ||--o{ PLACE_ALERT_TARGETS : targets
  PLACES ||--o{ PLACE_EVENTS : produces
  USERS ||--o{ PLACE_EVENTS : triggers

  USERS ||--o{ DRIVES : makes
  DRIVES ||--o{ DRIVE_EVENTS : contains

  USERS ||--o{ SOS_EVENTS : triggers
  CIRCLES ||--o{ SOS_EVENTS : alerts
  INCIDENT_DETECTIONS }o--|| SOS_EVENTS : linked_to

  CIRCLES ||--o{ CIRCLE_MESSAGES : has
  USERS ||--o{ CIRCLE_MESSAGES : sends
  CIRCLE_MESSAGES ||--o{ CIRCLE_MESSAGE_RECEIPTS : receipt

  CIRCLES ||--o{ CHECKINS : receives
  USERS ||--o{ CHECKINS : creates

  USERS ||--o{ NOTIFICATION_PREFERENCES : configures
  USERS ||--o{ NOTIFICATIONS : receives

  PLANS ||--o{ SUBSCRIPTIONS : used_by
  USERS ||--o{ SUBSCRIPTIONS : owns

  ADMIN_USERS ||--o{ USER_FLAGS : creates
  ADMIN_USERS ||--o{ AUDIT_LOGS : writes
```
