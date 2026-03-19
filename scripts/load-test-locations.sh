#!/usr/bin/env bash
# ============================================================================
# Load Test — POST /api/v1/locations/stream
# ============================================================================
# Simula um dispositivo se movendo e enviando lotes de localização.
#
# USO:
#   ./scripts/load-test-locations.sh --access-token "<jwt>" --total-requests 100
#   ./scripts/load-test-locations.sh --access-token "<jwt>" --circle-id "<uuid>" --total-requests 200 --events-per-request 5 --delay-ms 100
#   ./scripts/load-test-locations.sh --access-token "<jwt>" --total-requests 50 --parallel 4
#
# PARÂMETROS:
#   --access-token      (obrigatório) JWT de autenticação
#   --circle-id         (opcional)    UUID do círculo
#   --base-url          (opcional)    URL base da API (default: http://localhost:8080)
#   --total-requests    (opcional)    Quantidade total de requests (default: 50)
#   --events-per-request(opcional)    Eventos por request (default: 3)
#   --delay-ms          (opcional)    Delay entre requests em ms (default: 200)
#   --parallel          (opcional)    Requests em paralelo (default: 1, sequencial)
# ============================================================================

set -u

ACCESS_TOKEN=""
CIRCLE_ID=""
BASE_URL="http://localhost:8080"
TOTAL_REQUESTS=50
EVENTS_PER_REQUEST=3
DELAY_MS=200
PARALLEL=1

BASE_LAT="-23.561414"
BASE_LON="-46.655881"
SOURCES=("GPS" "NETWORK" "FUSED")

usage() {
  cat <<'EOF'
Uso:
  ./scripts/load-test-locations.sh --access-token "<jwt>" [opcoes]

Opcoes:
  --access-token <token>       JWT de autenticacao (obrigatorio)
  --circle-id <uuid>           UUID do circulo
  --base-url <url>             URL base da API (default: http://localhost:8080)
  --total-requests <n>         Quantidade total de requests (default: 50)
  --events-per-request <n>     Eventos por request (default: 3)
  --delay-ms <n>               Delay entre requests em ms (default: 200)
  --parallel <n>               Requests em paralelo (default: 1)
  -h, --help                   Mostra esta ajuda

Dependencias:
  curl, jq, awk, date
EOF
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Erro: comando obrigatorio nao encontrado: $1" >&2
    exit 1
  fi
}

is_integer() {
  [[ "$1" =~ ^[0-9]+$ ]]
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --access-token)
        ACCESS_TOKEN="${2:-}"
        shift 2
        ;;
      --circle-id)
        CIRCLE_ID="${2:-}"
        shift 2
        ;;
      --base-url)
        BASE_URL="${2:-}"
        shift 2
        ;;
      --total-requests)
        TOTAL_REQUESTS="${2:-}"
        shift 2
        ;;
      --events-per-request)
        EVENTS_PER_REQUEST="${2:-}"
        shift 2
        ;;
      --delay-ms)
        DELAY_MS="${2:-}"
        shift 2
        ;;
      --parallel)
        PARALLEL="${2:-}"
        shift 2
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "Erro: parametro desconhecido: $1" >&2
        usage
        exit 1
        ;;
    esac
  done
}

validate_args() {
  if [[ -z "$ACCESS_TOKEN" ]]; then
    echo "Erro: --access-token e obrigatorio." >&2
    usage
    exit 1
  fi

  for n in "$TOTAL_REQUESTS" "$EVENTS_PER_REQUEST" "$DELAY_MS" "$PARALLEL"; do
    if ! is_integer "$n"; then
      echo "Erro: valor numerico invalido: $n" >&2
      exit 1
    fi
  done

  if (( TOTAL_REQUESTS <= 0 || EVENTS_PER_REQUEST <= 0 || PARALLEL <= 0 )); then
    echo "Erro: --total-requests, --events-per-request e --parallel devem ser > 0." >&2
    exit 1
  fi
}

calc_value() {
  local req_idx="$1"
  local event_idx="$2"
  local step="$3"

  awk -v base="$4" -v r="$req_idx" -v e="$event_idx" -v c="$EVENTS_PER_REQUEST" -v s="$step" 'BEGIN {
    gi=(r*c)+e
    printf "%.6f", (base + (gi * s))
  }'
}

random_float_1() {
  local min="$1"
  local max="$2"
  awk -v min="$min" -v max="$max" 'BEGIN {
    srand();
    printf "%.1f", (min + rand() * (max - min))
  }'
}

random_int() {
  local min="$1"
  local max="$2"
  awk -v min="$min" -v max="$max" 'BEGIN {
    srand();
    printf "%d", int(min + rand() * (max - min + 1))
  }'
}

build_payload() {
  local req_idx="$1"
  local events_json="[]"

  local i
  for (( i=0; i<EVENTS_PER_REQUEST; i++ )); do
    local lat lon speed heading battery src is_moving acc alt ts src_idx

    lat="$(calc_value "$req_idx" "$i" "0.0001" "$BASE_LAT")"
    lon="$(calc_value "$req_idx" "$i" "0.00005" "$BASE_LON")"
    speed="$(random_float_1 0.0 15.0)"
    heading="$(random_int 0 359)"
    battery="$(random_int 20 100)"
    src_idx="$(random_int 0 2)"
    src="${SOURCES[$src_idx]}"
    acc="$(random_float_1 3.0 25.0)"
    alt="$(random_float_1 700.0 800.0)"

    is_moving="false"
    awk -v v="$speed" 'BEGIN { if (v > 0.5) exit 0; exit 1 }'
    if [[ $? -eq 0 ]]; then
      is_moving="true"
    fi

    local seconds_back=$((EVENTS_PER_REQUEST - i))
    ts="$(date -u -d "-${seconds_back} seconds" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")"

    local event_json
    event_json="$(jq -nc \
      --argjson latitude "$lat" \
      --argjson longitude "$lon" \
      --argjson accuracyMeters "$acc" \
      --argjson speedMps "$speed" \
      --argjson headingDegrees "$heading" \
      --argjson altitudeMeters "$alt" \
      --arg source "$src" \
      --arg recordedAt "$ts" \
      --argjson isMoving "$is_moving" \
      --argjson batteryLevel "$battery" \
      '{
        latitude: $latitude,
        longitude: $longitude,
        accuracyMeters: $accuracyMeters,
        speedMps: $speedMps,
        headingDegrees: $headingDegrees,
        altitudeMeters: $altitudeMeters,
        source: $source,
        recordedAt: $recordedAt,
        isMoving: $isMoving,
        batteryLevel: $batteryLevel
      }')"

    events_json="$(jq -c --argjson e "$event_json" '. + [$e]' <<<"$events_json")"
  done

  if [[ -n "$CIRCLE_ID" ]]; then
    jq -nc --arg circleId "$CIRCLE_ID" --argjson events "$events_json" '{circleId: $circleId, events: $events}'
  else
    jq -nc --argjson events "$events_json" '{events: $events}'
  fi
}

send_batch() {
  local req_idx="$1"
  local url="$BASE_URL/api/v1/locations/stream"
  local payload
  payload="$(build_payload "$req_idx")"

  local start_ns end_ns elapsed_ms
  start_ns="$(date +%s%N 2>/dev/null || echo "$(($(date +%s) * 1000000000))")"

  local tmp_code tmp_out http_code
  tmp_code="$(mktemp)"
  tmp_out="$(mktemp)"

  curl -sS -o "$tmp_out" -w "%{http_code}" \
    -X POST "$url" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    --data "$payload" >"$tmp_code" 2>/dev/null
  local curl_exit=$?

  end_ns="$(date +%s%N 2>/dev/null || echo "$(($(date +%s) * 1000000000))")"
  elapsed_ms=$(( (end_ns - start_ns) / 1000000 ))

  http_code="$(cat "$tmp_code")"
  rm -f "$tmp_code" "$tmp_out"

  local success=0
  if [[ $curl_exit -eq 0 && "$http_code" =~ ^2[0-9][0-9]$ ]]; then
    success=1
  fi

  # Formato: index|status|timeMs|success
  echo "$((req_idx + 1))|${http_code:-0}|$elapsed_ms|$success"
}

print_header() {
  local url="$BASE_URL/api/v1/locations/stream"
  printf "\n========================================\n"
  printf " Load Test - Location Stream\n"
  printf "========================================\n"
  printf "  URL:              %s\n" "$url"
  printf "  Total Requests:   %d\n" "$TOTAL_REQUESTS"
  printf "  Events/Request:   %d\n" "$EVENTS_PER_REQUEST"
  printf "  Total Events:     %d\n" "$((TOTAL_REQUESTS * EVENTS_PER_REQUEST))"
  printf "  Delay:            %dms\n" "$DELAY_MS"
  printf "  Parallel:         %d\n" "$PARALLEL"
  if [[ -n "$CIRCLE_ID" ]]; then
    printf "  CircleId:         %s\n" "$CIRCLE_ID"
  fi
  printf "========================================\n\n"
}

print_result_line() {
  local idx="$1"
  local total="$2"
  local status="$3"
  local time_ms="$4"
  printf "  [%4d/%d] HTTP %s  %sms\n" "$idx" "$total" "$status" "$time_ms"
}

main() {
  require_cmd curl
  require_cmd jq
  require_cmd awk
  require_cmd date

  parse_args "$@"
  validate_args

  print_header

  local start_s end_s total_ms
  start_s="$(date +%s%N 2>/dev/null || echo "$(($(date +%s) * 1000000000))")"

  local tmp_results
  tmp_results="$(mktemp)"

  if (( PARALLEL <= 1 )); then
    local i
    for (( i=0; i<TOTAL_REQUESTS; i++ )); do
      local r
      r="$(send_batch "$i")"
      echo "$r" >>"$tmp_results"

      IFS='|' read -r idx status tms success <<<"$r"
      print_result_line "$idx" "$TOTAL_REQUESTS" "$status" "$tms"

      if (( DELAY_MS > 0 && i < TOTAL_REQUESTS - 1 )); then
        sleep "$(awk -v d="$DELAY_MS" 'BEGIN { printf "%.3f", d/1000 }')"
      fi
    done
  else
    local running=0
    local i
    for (( i=0; i<TOTAL_REQUESTS; i++ )); do
      (
        send_batch "$i"
      ) >>"$tmp_results" &

      running=$((running + 1))
      if (( running >= PARALLEL )); then
        wait -n
        running=$((running - 1))
      fi
    done
    wait

    sort -t'|' -n -k1,1 "$tmp_results" -o "$tmp_results"

    while IFS='|' read -r idx status tms success; do
      print_result_line "$idx" "$TOTAL_REQUESTS" "$status" "$tms"
    done <"$tmp_results"
  fi

  end_s="$(date +%s%N 2>/dev/null || echo "$(($(date +%s) * 1000000000))")"
  total_ms=$(( (end_s - start_s) / 1000000 ))

  local success_count fail_count avg_ms min_ms max_ms p95 rps
  success_count="$(awk -F'|' '$4==1 {c++} END {print c+0}' "$tmp_results")"
  fail_count=$((TOTAL_REQUESTS - success_count))

  avg_ms="$(awk -F'|' '{s+=$3; c++} END { if (c>0) printf "%.1f", s/c; else print "0.0" }' "$tmp_results")"
  min_ms="$(awk -F'|' 'NR==1{m=$3} $3<m{m=$3} END{print m+0}' "$tmp_results")"
  max_ms="$(awk -F'|' 'NR==1{m=$3} $3>m{m=$3} END{print m+0}' "$tmp_results")"

  local count p95_pos
  count="$(wc -l <"$tmp_results" | tr -d ' ')"
  p95_pos=$(( (count * 95) / 100 ))
  if (( p95_pos < 1 )); then p95_pos=1; fi
  p95="$(awk -F'|' '{print $3}' "$tmp_results" | sort -n | sed -n "${p95_pos}p")"
  p95="${p95:-0}"

  rps="$(awk -v n="$TOTAL_REQUESTS" -v ms="$total_ms" 'BEGIN { if (ms>0) printf "%.1f", (n/(ms/1000)); else print "0.0" }')"

  printf "\n========================================\n"
  printf " Resultados\n"
  printf "========================================\n"
  printf "  Total Time:       %.2fs\n" "$(awk -v ms="$total_ms" 'BEGIN {print ms/1000}')"
  printf "  Requests/sec:     %s\n" "$rps"
  printf "  Success:          %d / %d\n" "$success_count" "$TOTAL_REQUESTS"
  if (( fail_count > 0 )); then
    printf "  Failed:           %d\n" "$fail_count"
  fi
  printf "  Avg Latency:      %sms\n" "$avg_ms"
  printf "  Min Latency:      %sms\n" "$min_ms"
  printf "  Max Latency:      %sms\n" "$max_ms"
  printf "  P95 Latency:      %sms\n" "$p95"
  printf "  Total Events:     %d\n" "$((TOTAL_REQUESTS * EVENTS_PER_REQUEST))"
  printf "========================================\n\n"

  rm -f "$tmp_results"
}

main "$@"
