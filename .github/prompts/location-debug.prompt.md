---
name: location-debug
description: "Use quando investigar problemas no domínio de location do Locator360, especialmente stream de localização, publicação de eventos, consumers downstream e observabilidade do pipeline. Palavras-chave: location, locations stream, debug location, geofence, kafka, drive detection, onboarding location."
---

# Debug Location

Use este prompt quando o problema estiver no pipeline de localização e você precisar iniciar a investigação com contexto do projeto.

## Objetivo

Antes de sugerir correções, identifique em qual etapa do pipeline de localização está a falha e proponha o próximo passo de diagnóstico com menor custo.

## O que você deve fazer

1. Identificar o sintoma principal.
2. Consultar as docs, scripts e instructions relevantes.
3. Mapear o pipeline provável afetado.
4. Sugerir o melhor agent ou skill para continuar a investigação.
5. Propor os próximos passos mínimos de debug.

## Referências prioritárias

- commands.md
- scripts/load-test-locations.sh
- Docs/location-tracking-strategy.md
- Docs/detection-strategy.md
- Docs/detection-pseudocode.md
- Docs/copilot-agents-and-skills-guide.md
- .github/instructions/observability.instructions.md
- .github/instructions/testing.instructions.md

## Pipeline a considerar

1. Endpoint HTTP de stream de localização.
2. Use case de ingestão.
3. Publicação em `location.events`.
4. Consumers downstream como geofence e drive detection.
5. Logs, métricas e traces no LGTM.

## Formato de resposta

### Leitura inicial

- Sintoma principal
- Etapa provável da falha
- Evidências iniciais

### Melhor caminho

- Agent recomendado
- Skills de apoio
- Justificativa curta

### Próximos passos

Liste os 3 a 5 próximos passos de diagnóstico mais úteis.

## Restrições

- Não sugerir mudanças antes de localizar a etapa provável da falha.
- Não ignorar infraestrutura, Kafka e observabilidade.
- Não usar `mvn` local para validação Java.
