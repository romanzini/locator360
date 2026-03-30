# Estratégia de Detecção de Viagens (Drives) e Eventos de Direção

 Este documento resume como popular as tabelas `drives` e `drive_events` a partir do fluxo de localização (`locations`), com base nos requisitos já descritos em:

- especificacao-funcional.md (Módulo de Direção & Segurança no Trânsito)
- backlog.md (Épico 6 – Direção e Segurança no Trânsito, US-050/051/052)
- location-tracking-strategy.md (estratégia de coleta e envio de localização)
- database-model.md (definições das tabelas `drives` e `drive_events`)

---

## 1. Visão Geral

Entrada principal: sequência de registros em `locations` para um `user_id` (e opcionalmente `circle_id`), ordenados por `recorded_at`.

Objetivos:

- Identificar automaticamente **viagens veiculares** e gravar um resumo em `drives` com `mode = CAR`.
- Detectar **eventos de comportamento de direção** (ex.: excesso de velocidade, frenagens bruscas) e gravá-los em `drive_events` associados a um `drive` veicular.

Parâmetros iniciais (ajustáveis por experimento):

- Para viagens veiculares (`mode = CAR`):
  - `V_DRIVE_MIN` – limiar de velocidade para considerar deslocamento veicular (ex.: ~20 km/h ≈ 5.5 m/s).
  - `T_DRIVE_START` – tempo mínimo com velocidade ≥ `V_DRIVE_MIN` para abrir uma viagem.
  - `T_DRIVE_END` – tempo parado ou em baixa velocidade para encerrar a viagem.

---

## 2. Populando a Tabela `drives`

Estado por usuário:

- `current_drive_id` – id da viagem aberta (ou `null` se não houver).
- `current_mode` – modo de deslocamento detectado para o drive atual (ex.: `CAR`).
- Último ponto de localização conhecido.

### 2.1. Abertura de uma viagem veicular

Quando **não** existe viagem aberta (`current_drive_id = null`):

1. A cada novo registro em `locations` para um usuário:
   - Considerar `speed_mps` e `recorded_at`.
   - Se a velocidade se mantiver **acima de `V_DRIVE_MIN`** por pelo menos `T_DRIVE_START` (usando uma janela de últimos pontos):
     - Criar registro em `drives` com os campos: `user_id`, `circle_id`, `mode = CAR`, `start_location_id`, `start_time`, `distance_meters = 0`, `duration_seconds = 0`, `max_speed_mps = speed_mps_inicial`, `avg_speed_mps = 0`, `safety_score` inicial (ex.: 100), `created_at` e `updated_at`.
   - Guardar `current_drive_id` e `current_mode` correspondente.

### 2.2. Atualização durante a viagem

Enquanto **existe** viagem aberta (`current_drive_id != null`):

1. Para cada novo `location` associado ao usuário:
   - Atualizar agregados da viagem:
     - `distance_meters` + distância entre o último ponto e o atual (Haversine).
     - `duration_seconds` = `recorded_at_atual - start_time`.
     - `max_speed_mps` = `max(max_speed_mps, speed_mps_atual)`.
     - `avg_speed_mps` = `distance_meters / duration_seconds` (quando `duration_seconds > 0`).
   - Invocar detectores de eventos de direção (seção 3) e popular `drive_events` (quando o modo for veicular).

### 2.3. Encerramento da viagem (por modo)

A viagem deve ser encerrada quando a velocidade fica **consistentemente abaixo de `V_DRIVE_MIN`** (ou zero) por pelo menos `T_DRIVE_END`, ou não chegam novos pontos por tempo maior que `T_DRIVE_END` (timeout).

Ao encerrar:

1. Definir `end_location_id` = último `locations.id` usado.
2. Definir `end_time` = `recorded_at` desse último ponto.
3. Recalcular/finalizar `distance_meters`, `duration_seconds`, `max_speed_mps`, `avg_speed_mps`.
4. Calcular `safety_score` com base nos eventos em `drive_events` ligados a esse `drive`.
5. Atualizar `updated_at`.
6. Limpar `current_drive_id`.

É recomendável um job de background que feche viagens "órfãs" caso algum edge case impeça o fechamento normal (ex.: app morto, último ponto sem parada clara).

---

## 3. Populando a Tabela `drive_events`

Enquanto existir uma viagem aberta (`current_drive_id`) com `mode = CAR`, cada novo `location` (e/ou snapshot de sensores) é inspecionado por diferentes detectores de eventos. Quando um evento é detectado, cria-se um registro em `drive_events`.

Campos principais de `drive_events`:

- `drive_id` (FK → `drives.id`).
- `user_id` (FK → `users.id`).
- `event_type` (ENUM: `SPEEDING`, `HARD_BRAKE`, `HARD_ACCEL`, `HARD_TURN`, `PHONE_USAGE`).
- `severity` (ENUM: `LOW`, `MEDIUM`, `HIGH`).
- `location_id` (FK → `locations.id`, nullable).
- `speed_mps` (opcional, conforme tipo).
- `occurred_at` (timestamp do evento).

### 3.1. Excesso de velocidade (`SPEEDING`)

- Definir limite de velocidade:
  - Global (ex.: 120 km/h) **ou** derivado de configurações por círculo (`circle_settings.driving_alert_level`).
- Se `speed_mps` > limite:
  - Criar `drive_events` com `event_type = SPEEDING` e `severity` baseada em quanto excede o limite.

### 3.2. Frenagem / aceleração brusca (`HARD_BRAKE`, `HARD_ACCEL`)

- Calcular variação de velocidade entre pontos: Δv/Δt.
- Se Δv/Δt ultrapassar thresholds definidos:
  - Criar evento com tipo adequado e `severity` proporcional ao valor.

### 3.3. Curva brusca (`HARD_TURN`)

- Avaliar variação de heading / direção (entre dois ou mais pontos ou via giroscópio).
- Acima de certo limiar, registrar `HARD_TURN` com severidade.

### 3.4. Uso de telefone (`PHONE_USAGE`)

- Se o app detectar uso de telefone em situação de direção (ex.: tela ativa + alta velocidade), enviar essa informação ao backend.
- O backend vincula esse evento ao `current_drive_id` e à localização correspondente.

---

## 4. Cálculo de `safety_score`

O campo `safety_score` em `drives` representa uma nota (ex.: 0–100) baseada nos eventos de direção ocorridos durante a viagem.

Abordagem simples inicial:

- Começar de 100 e subtrair pontos conforme:
  - Quantidade e severidade de `SPEEDING`.
  - Frenagens, acelerações e curvas bruscas.
  - Eventos de `PHONE_USAGE`.
- Garantir que o score fique limitado a [0, 100].

A fórmula exata é intencionalmente ajustável para calibrar o produto ao longo do tempo.

---

## 5. Resumo Operacional

- **Drives** são registros agregados de viagens veiculares, derivados de `locations` usando thresholds de velocidade e tempo, com `mode = CAR`.
- **Drive_events** são eventos pontuais de direção (risco/comportamento), sempre ligados a um `drive` em andamento.
- Toda a detecção roda em um serviço (backend ou no device com sincronização), que consome o fluxo de `locations` e aplica as regras descritas acima.
