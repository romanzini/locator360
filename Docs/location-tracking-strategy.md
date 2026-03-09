# Estratégia de Coleta e Envio de Geolocalização

## 1. Premissas

- Sempre com consentimento do usuário (permite localização + modo "compartilhar em background").
- Toda posição é enviada via `POST /locations/stream` em lote, conforme o OpenAPI.
- O app mantém uma **fila local** de pontos de localização para aguentar períodos sem rede.

---

## 2. Quando ligar/desligar o tracking

- **Ligar tracking** quando:
  - Usuário faz login;
  - Usuário habilita "compartilhar minha localização" nas configurações.
- **Desligar/pausar** quando:
  - Usuário desativa compartilhamento;
  - Faz logout;
  - Ou o app detecta que a permissão do SO permite apenas "quando em uso" (respeitar permissão).

---

## 3. Coleta de localização (lógica de amostragem)

Padrão inicial sugerido (ajustável por experimento):

- **Em movimento (carro / deslocamento rápido)**:
  - Intervalo alvo entre leituras: ~30–60 segundos.
  - Distância mínima entre pontos: ~50–100 metros.
- **Caminhando / movimento lento**:
  - Intervalo alvo: ~1–3 minutos.
  - Distância mínima: ~50–100 metros.
- **Parado (pouca variação)**:
  - Intervalo alvo: ~10–15 minutos;
  - Idealmente usar APIs de "significant change" / geofences para ser acordado só quando realmente se mover.

Na prática, o app usa as APIs do sistema operacional (direto ou via biblioteca de RN/Flutter/etc.) para:

- Ajustar intervalos / prioridade (alta precisão vs economia de energia).
- Deixar o sistema otimizar a frequência real para economizar bateria.

---

## 4. Estratégia de batching e envio para o backend

- Manter em memória + armazenamento local (ex.: SQLite) uma lista de `LocationEventInput`.
- Enviar via `POST /locations/stream` quando ocorrer qualquer um destes gatilhos:
  - **N pontos acumulados** (ex.: 10–20 eventos);
  - **T tempo passado** desde o último envio (ex.: 60–120 segundos);
  - **Mudança de rede** (ficou online depois de offline);
  - **App volta para foreground** (aproveita para "drenar" a fila).
- Se o `POST /locations/stream` falhar:
  - Não apagar a fila local;
  - Tentar novamente depois (backoff simples: 30s, 1min, 2min...).

Isso reduz chamadas, economiza bateria e rede, e casa bem com o formato em lote definido na API.

---

## 5. Background (conceito)

- **Android**:
  - Usar um serviço de localização em primeiro plano (Foreground Service) + agendador (WorkManager) para garantir que, mesmo se a UI for encerrada, o serviço continue até certo ponto.
  - Requer permissões de localização precisa e em background.
- **iOS**:
  - Habilitar "Background Modes" para localização.
  - Solicitar permissão do tipo "Always" (ou equivalente atual do iOS).
  - Usar APIs de `startUpdatingLocation`, `significant location changes` e/ou geofences.

Em ambos os casos, o código que roda em background segue a mesma lógica:

1. Recebe ponto(s) de localização do SO.
2. Coloca na fila local.
3. Dispara envio em lote quando for oportuno (segundo as regras da seção 4).

---

## 6. Mapeamento para o backend Locator 360

Cada evento gerado pelo app vira um item em `events[]` no `POST /locations/stream`, contendo, por exemplo:

- `latitude`, `longitude`;
- `recordedAt` (horário local/UTC);
- `accuracy` (se disponível);
- `speed` ou `isMoving` (opcional, ajuda a diferenciar parado/dirigindo);
- `source` (GPS, fused, etc.);
- `circleId` opcional (círculo de contexto atual).

---

## 7. Política simples inicial para o Locator 360

- Coleta **contínua** enquanto o usuário:
  - Está logado; e
  - Não desativou o compartilhamento de localização.
- Envio **em lote** a cada:
  - 10–20 pontos; **ou**
  - 60–120 segundos (o que vier primeiro).
- Em background:
  - Tentar manter a mesma política;
  - Deixar o sistema operacional reduzir a frequência quando o app estiver ocioso, para economizar bateria.

---

## 8. Pseudo-código do loop de coleta e envio

Pseudo-código genérico (independente de plataforma) para o fluxo de coleta → fila → envio:

```pseudo
// Variáveis principais
trackingEnabled = false
eventQueue = []
lastSendAt = now()

function onUserLogin() {
  trackingEnabled = true
  startLocationUpdates() // registra callbacks de localização no SO
}

function onUserLogoutOrStopSharing() {
  trackingEnabled = false
  stopLocationUpdates()
}

// Callback chamado pelo SO quando há nova localização
function onLocationUpdate(location) {
  if (!trackingEnabled) return

  eventQueue.push(location)

  if (eventQueue.length >= MAX_EVENTS || timeSince(lastSendAt) >= MAX_INTERVAL) {
    trySendBatch()
  }
}

// Tentativa de envio em lote
function trySendBatch() {
  if (!trackingEnabled) return
  if (!hasNetworkConnection()) return
  if (eventQueue.isEmpty()) return

  batch = eventQueue.peekFirstN(MAX_EVENTS) // não remove ainda

  body = {
    circleId: currentCircleIdOrNull(),
    events: mapToLocationEventInput(batch)
  }

  response = httpPost("/locations/stream", body, authToken)

  if (response.isSuccess()) {
    eventQueue.removeFirstN(batch.length)
    lastSendAt = now()
  } else {
    // mantém os eventos na fila e tenta depois (backoff)
    scheduleRetryWithBackoff()
  }
}

// Timer/job periódico em background
function onTimerTick() {
  if (!trackingEnabled) return
  if (!eventQueue.isEmpty()) {
    trySendBatch()
  }
}
```

---

## 9. Diagrama de fluxo do loop de tracking

Representação em Mermaid do fluxo principal de coleta → fila → envio:

```mermaid
flowchart TD
  A[Usuário faz login e habilita sharing] --> B[trackingEnabled = true]
  B --> C[SO entrega nova localização (onLocationUpdate)]

  C --> D[Adicionar localização em eventQueue]
  D --> E{Fila atingiu N eventos
          OU passou MAX_INTERVAL?}

  E -- Não --> F[Esperar próxima atualização ou timer]
  F --> C

  E -- Sim --> G[trySendBatch()]

  G --> H{Há rede disponível?}
  H -- Não --> I[Manter eventos na fila
                  e tentar depois]
  I --> F

  H -- Sim --> J[Montar body /locations/stream
                 com events[]]
  J --> K[HTTP POST /locations/stream]
  K --> L{Resposta de sucesso?}

  L -- Sim --> M[Remover eventos enviados da fila
                 lastSendAt = now()]
  M --> F

  L -- Não --> I

  %% Logout ou stop sharing
  N[Usuário faz logout
     ou desativa sharing] --> O[trackingEnabled = false
                               stopLocationUpdates()]
```
