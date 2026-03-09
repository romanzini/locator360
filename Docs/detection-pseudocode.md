# Pseudo-código de Detecção de Viagens (Drives) e Eventos de Direção

```pseudo
// Parâmetros (ajustáveis) para drives de carro (mode = CAR)
V_DRIVE_MIN = 5.5      // ~20 km/h em m/s
T_DRIVE_START = 30s    // tempo mínimo acima de V_DRIVE_MIN para abrir viagem
T_DRIVE_END = 5min     // tempo parado/baixo para fechar viagem

// Estado em memória por usuário
state[userId] = {
  currentDriveId: null,
  currentMode: null,        // ex.: "CAR"
  lastLocation: null,
  lastDriveMovingAt: null,
  lastLowSpeedAt: null
}

// Função chamada para cada novo location já persistido em `locations`
function onNewLocation(loc) {
  s = state[loc.user_id] or initState()
  now = loc.recorded_at

  if s.currentDriveId == null then
    handleNoOpenDrive(s, loc)
  else
    handleOpenDrive(s, loc)
}

// Sem drive aberto: decidir se abrimos um (veicular)
function handleNoOpenDrive(s, loc) {
  // Candidato a drive veicular
  if loc.speed_mps >= V_DRIVE_MIN then
    if s.lastDriveMovingAt == null then
      s.lastDriveMovingAt = loc.recorded_at
    end

    if (loc.recorded_at - s.lastDriveMovingAt) >= T_DRIVE_START then
      driveId = createDriveFromStart(loc, "CAR")
      s.currentDriveId = driveId
      s.currentMode = "CAR"
      s.lastLowSpeedAt = null
    end
  else
    s.lastDriveMovingAt = null
  end

  s.lastLocation = loc
}

// Com drive aberto: atualizar agregados, eventos e checar fim
function handleOpenDrive(s, loc) {
  drive = loadDrive(s.currentDriveId)

  // 1) Atualiza agregados da viagem
  if s.lastLocation != null then
    d = distance(s.lastLocation, loc)           // Haversine
    dt = loc.recorded_at - drive.start_time
    drive.distance_meters += d
    drive.duration_seconds = seconds(dt)
    drive.max_speed_mps = max(drive.max_speed_mps, loc.speed_mps)
    if drive.duration_seconds > 0 then
      drive.avg_speed_mps = drive.distance_meters / drive.duration_seconds
    end
  end

  saveDrive(drive)

  // 2) Detecta eventos de direção (drive_events) apenas para modos veiculares
  if s.currentMode == "CAR" then
    detectAndCreateDriveEvents(drive.id, s.lastLocation, loc)
  end

  // 3) Checa condições de encerramento
  thresholdSpeed = V_DRIVE_MIN
  thresholdTime = T_DRIVE_END

  if loc.speed_mps < thresholdSpeed then
    if s.lastLowSpeedAt == null then
      s.lastLowSpeedAt = loc.recorded_at
    end
  else
    s.lastLowSpeedAt = null
  end

  if s.lastLowSpeedAt != null and
     (loc.recorded_at - s.lastLowSpeedAt) >= thresholdTime then
    closeDrive(drive, loc)
    s.currentDriveId = null
    s.currentMode = null
    s.lastLowSpeedAt = null
  end

  s.lastLocation = loc
}

// Cria o registro inicial em `drives`
function createDriveFromStart(loc, mode) -> driveId {
  drive = {
    user_id: loc.user_id,
    circle_id: loc.circle_id,
    mode: mode,
    start_location_id: loc.id,
    end_location_id: null,
    start_time: loc.recorded_at,
    end_time: null,
    distance_meters: 0,
    duration_seconds: 0,
    max_speed_mps: loc.speed_mps,
    avg_speed_mps: 0,
    safety_score: 100,
    created_at: now(),
    updated_at: now()
  }
  return insertDrive(drive)
}

// Fecha o drive com último ponto
function closeDrive(drive, lastLoc) {
  drive.end_location_id = lastLoc.id
  drive.end_time = lastLoc.recorded_at
  if drive.mode == "CAR" then
    drive.safety_score = computeSafetyScore(drive.id)
  end
  drive.updated_at = now()
  saveDrive(drive)
}

// Detectores de eventos de direção
function detectAndCreateDriveEvents(driveId, prevLoc, loc) {
  if prevLoc == null then return

  // 1) SPEEDING
  limit = resolveSpeedLimit(loc)  // via config/círculo/etc
  if loc.speed_mps > limit then
    severity = classifySpeedSeverity(loc.speed_mps, limit)
    createDriveEvent(driveId, loc, "SPEEDING", severity)
  end

  // 2) HARD_BRAKE / HARD_ACCEL
  dv = loc.speed_mps - prevLoc.speed_mps
  dt = max(1, seconds(loc.recorded_at - prevLoc.recorded_at))
  accel = dv / dt

  if accel <= HARD_BRAKE_THRESHOLD then
    severity = classifyBrakeSeverity(accel)
    createDriveEvent(driveId, loc, "HARD_BRAKE", severity)
  else if accel >= HARD_ACCEL_THRESHOLD then
    severity = classifyAccelSeverity(accel)
    createDriveEvent(driveId, loc, "HARD_ACCEL", severity)
  end

  // 3) HARD_TURN (variação de heading)
  if loc.heading_degrees != null and prevLoc.heading_degrees != null then
    deltaHeading = normalizeAngle(loc.heading_degrees - prevLoc.heading_degrees)
    if abs(deltaHeading) >= HARD_TURN_ANGLE_THRESHOLD and loc.speed_mps >= V_MIN then
      severity = classifyTurnSeverity(deltaHeading, loc.speed_mps)
      createDriveEvent(driveId, loc, "HARD_TURN", severity)
    end
  end

  // 4) PHONE_USAGE (se vier marcado do app)
  if loc.phone_usage_flag == true then
    severity = PHONE_USAGE_SEVERITY_DEFAULT
    createDriveEvent(driveId, loc, "PHONE_USAGE", severity)
  end
}

// Inserção em `drive_events`
function createDriveEvent(driveId, loc, type, severity) {
  event = {
    drive_id: driveId,
    user_id: loc.user_id,
    event_type: type,
    severity: severity,
    location_id: loc.id,
    speed_mps: loc.speed_mps,
    occurred_at: loc.recorded_at,
    created_at: now()
  }
  insertDriveEvent(event)
}

// Exemplo de cálculo simples de safety_score (0–100)
function computeSafetyScore(driveId) -> number {
  events = loadDriveEvents(driveId)
  score = 100

  for e in events:
    if e.event_type == "SPEEDING" then
      score -= weightSpeeding(e.severity)
    else if e.event_type == "HARD_BRAKE" then
      score -= weightHardBrake(e.severity)
    else if e.event_type == "HARD_ACCEL" then
      score -= weightHardAccel(e.severity)
    else if e.event_type == "HARD_TURN" then
      score -= weightHardTurn(e.severity)
    else if e.event_type == "PHONE_USAGE" then
      score -= weightPhoneUsage(e.severity)
    end
  end

  return clamp(score, 0, 100)
}
```
