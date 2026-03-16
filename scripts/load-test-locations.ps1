# ============================================================================
# Load Test — POST /api/v1/locations/stream
# ============================================================================
# Simula um dispositivo se movendo e enviando lotes de localização.
#
# USO:
#   .\scripts\load-test-locations.ps1 -AccessToken "<jwt>" -TotalRequests 100
#   .\scripts\load-test-locations.ps1 -AccessToken "<jwt>" -CircleId "<uuid>" -TotalRequests 200 -EventsPerRequest 5 -DelayMs 100
#   .\scripts\load-test-locations.ps1 -AccessToken "<jwt>" -TotalRequests 50 -Parallel 4
#
# PARÂMETROS:
#   -AccessToken       (obrigatório) JWT de autenticação
#   -CircleId          (opcional)    UUID do círculo
#   -BaseUrl           (opcional)    URL base da API (default: http://localhost:8080)
#   -TotalRequests     (opcional)    Quantidade total de requests (default: 50)
#   -EventsPerRequest  (opcional)    Eventos por request (default: 3)
#   -DelayMs           (opcional)    Delay entre requests em ms (default: 200)
#   -Parallel          (opcional)    Requests em paralelo (default: 1, sequencial)
# ============================================================================

param(
  [Parameter(Mandatory = $true)]
  [string]$AccessToken,

  [string]$CircleId = "",
  [string]$BaseUrl = "http://localhost:8080",
  [int]$TotalRequests = 50,
  [int]$EventsPerRequest = 3,
  [int]$DelayMs = 200,
  [int]$Parallel = 1
)

$url = "$BaseUrl/api/v1/locations/stream"
$sources = @("GPS", "NETWORK", "FUSED")

# Ponto de partida: São Paulo, Av. Paulista
$baseLat = -23.561414
$baseLon = -46.655881

function New-LocationPayload {
  param([int]$RequestIndex, [int]$EventCount)

  $events = @()
  for ($i = 0; $i -lt $EventCount; $i++) {
    $globalIndex = ($RequestIndex * $EventCount) + $i
    # Simula movimento: desloca ~11m por evento (0.0001 grau)
    $lat = $baseLat + ($globalIndex * 0.0001)
    $lon = $baseLon + ($globalIndex * 0.00005)
    $speed = [math]::Round((Get-Random -Minimum 0.0 -Maximum 15.0), 1)
    $heading = Get-Random -Minimum 0 -Maximum 360
    $battery = Get-Random -Minimum 20 -Maximum 100
    $source = $sources[(Get-Random -Minimum 0 -Maximum $sources.Count)]
    $isMoving = $speed -gt 0.5
    $timestamp = (Get-Date).AddSeconds( - ($EventCount - $i)).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")

    $event = @{
      latitude       = [math]::Round($lat, 6)
      longitude      = [math]::Round($lon, 6)
      accuracyMeters = [math]::Round((Get-Random -Minimum 3.0 -Maximum 25.0), 1)
      speedMps       = $speed
      headingDegrees = $heading
      altitudeMeters = [math]::Round((Get-Random -Minimum 700.0 -Maximum 800.0), 1)
      source         = $source
      recordedAt     = $timestamp
      isMoving       = $isMoving
      batteryLevel   = $battery
    }
    $events += $event
  }

  $body = @{ events = $events }
  if ($CircleId -ne "") {
    $body.circleId = $CircleId
  }

  return ($body | ConvertTo-Json -Depth 5 -Compress)
}

function Send-LocationBatch {
  param([int]$Index)

  $payload = New-LocationPayload -RequestIndex $Index -EventCount $EventsPerRequest
  $sw = [System.Diagnostics.Stopwatch]::StartNew()
  try {
    $response = Invoke-WebRequest -Uri $url -Method POST `
      -ContentType "application/json" `
      -Headers @{ Authorization = "Bearer $AccessToken" } `
      -Body $payload `
      -UseBasicParsing
    $sw.Stop()
    return @{
      Index   = $Index + 1
      Status  = $response.StatusCode
      TimeMs  = $sw.ElapsedMilliseconds
      Success = $true
    }
  }
  catch {
    $sw.Stop()
    $statusCode = 0
    if ($_.Exception.Response) {
      $statusCode = [int]$_.Exception.Response.StatusCode
    }
    return @{
      Index   = $Index + 1
      Status  = $statusCode
      TimeMs  = $sw.ElapsedMilliseconds
      Success = $false
      Error   = $_.Exception.Message
    }
  }
}

# ── Header ──
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Load Test — Location Stream" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  URL:              $url"
Write-Host "  Total Requests:   $TotalRequests"
Write-Host "  Events/Request:   $EventsPerRequest"
Write-Host "  Total Events:     $($TotalRequests * $EventsPerRequest)"
Write-Host "  Delay:            ${DelayMs}ms"
Write-Host "  Parallel:         $Parallel"
if ($CircleId -ne "") { Write-Host "  CircleId:         $CircleId" }
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$results = @()
$totalSw = [System.Diagnostics.Stopwatch]::StartNew()

if ($Parallel -le 1) {
  # ── Modo sequencial ──
  for ($i = 0; $i -lt $TotalRequests; $i++) {
    $result = Send-LocationBatch -Index $i
    $results += $result
    $color = if ($result.Success) { "Green" } else { "Red" }
    Write-Host ("  [{0,4}/{1}] HTTP {2}  {3}ms" -f $result.Index, $TotalRequests, $result.Status, $result.TimeMs) -ForegroundColor $color

    if ($DelayMs -gt 0 -and $i -lt ($TotalRequests - 1)) {
      Start-Sleep -Milliseconds $DelayMs
    }
  }
}
else {
  # ── Modo paralelo (usando runspaces) ──
  $runspacePool = [runspacefactory]::CreateRunspacePool(1, $Parallel)
  $runspacePool.Open()
  $jobs = @()

  for ($i = 0; $i -lt $TotalRequests; $i++) {
    $ps = [powershell]::Create()
    $ps.RunspacePool = $runspacePool
    $ps.AddScript({
        param($Url, $Token, $Payload)
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        try {
          $r = Invoke-WebRequest -Uri $Url -Method POST `
            -ContentType "application/json" `
            -Headers @{ Authorization = "Bearer $Token" } `
            -Body $Payload -UseBasicParsing
          $sw.Stop()
          return @{ Status = $r.StatusCode; TimeMs = $sw.ElapsedMilliseconds; Success = $true }
        }
        catch {
          $sw.Stop()
          $sc = 0
          if ($_.Exception.Response) { $sc = [int]$_.Exception.Response.StatusCode }
          return @{ Status = $sc; TimeMs = $sw.ElapsedMilliseconds; Success = $false }
        }
      }).AddArgument($url).AddArgument($AccessToken).AddArgument((New-LocationPayload -RequestIndex $i -EventCount $EventsPerRequest)) | Out-Null

    $jobs += @{ PS = $ps; Handle = $ps.BeginInvoke(); Index = $i + 1 }
  }

  foreach ($job in $jobs) {
    $r = $job.PS.EndInvoke($job.Handle)
    $r[0].Index = $job.Index
    $results += $r[0]
    $color = if ($r[0].Success) { "Green" } else { "Red" }
    Write-Host ("  [{0,4}/{1}] HTTP {2}  {3}ms" -f $r[0].Index, $TotalRequests, $r[0].Status, $r[0].TimeMs) -ForegroundColor $color
    $job.PS.Dispose()
  }
  $runspacePool.Close()
}

$totalSw.Stop()

# ── Resumo ──
$successCount = ($results | Where-Object { $_.Success }).Count
$failCount = $TotalRequests - $successCount
$times = $results | ForEach-Object { $_.TimeMs }
$avgMs = [math]::Round(($times | Measure-Object -Average).Average, 1)
$minMs = ($times | Measure-Object -Minimum).Minimum
$maxMs = ($times | Measure-Object -Maximum).Maximum
$p95 = ($times | Sort-Object)[([math]::Floor($times.Count * 0.95))]
$rps = [math]::Round($TotalRequests / ($totalSw.ElapsedMilliseconds / 1000.0), 1)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Resultados" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Total Time:       $([math]::Round($totalSw.ElapsedMilliseconds / 1000.0, 2))s"
Write-Host "  Requests/sec:     $rps"
Write-Host "  Success:          $successCount / $TotalRequests" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
if ($failCount -gt 0) {
  Write-Host "  Failed:           $failCount" -ForegroundColor Red
}
Write-Host "  Avg Latency:      ${avgMs}ms"
Write-Host "  Min Latency:      ${minMs}ms"
Write-Host "  Max Latency:      ${maxMs}ms"
Write-Host "  P95 Latency:      ${p95}ms"
Write-Host "  Total Events:     $($TotalRequests * $EventsPerRequest)"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
