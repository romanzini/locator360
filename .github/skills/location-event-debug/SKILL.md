---
name: location-event-debug
description: "Use quando depurar ingestão de localização ou fluxo de eventos no Locator360. Palavras-chave: stream de locations, problema de geofence, problema de Kafka, depuração de eventos de localização, teste de carga de locations, tracing do pipeline de localização, ingestão de GPS."
---

# Location Event Debug

Use esta skill para o pipeline de localização, da ingestão HTTP ao processamento assíncrono.

## Caminho principal a inspecionar

1. POST /api/v1/locations/stream
2. Application use case for ingestion
3. Kafka publish to location.events
4. Consumers such as geofence or drive detection
5. Logs, metrics, and traces in Grafana LGTM

## Assets úteis do repositório

- commands.md
- scripts/load-test-locations.sh
- docker-compose.infra.yml
- Docs/location-tracking-strategy.md
- Docs/detection-strategy.md

## Workflow de depuração

1. Confirme que a infra está de pé.
2. Confirme que a aplicação está rodando com o ambiente esperado.
3. Reproduza com uma request mínima ou com o script de carga.
4. Verifique logs do controller e do service.
5. Verifique o fluxo no tópico Kafka.
6. Verifique os consumers downstream.
7. Verifique traces ou métricas se os logs forem inconclusivos.

## Saída

- Resumo do sintoma
- Etapa do pipeline onde falha
- Evidências
- Próximo passo concreto de diagnóstico ou correção

## Prompt relacionado

- Use `.github/prompts/location-debug.prompt.md` quando quiser iniciar diretamente pela investigação do pipeline de localização com contexto especializado.
