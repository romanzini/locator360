---
name: critical-flow
description: "Use quando a tarefa envolver um fluxo crítico do Locator360, como auth, sos ou billing, e exigir atenção extra a regressão, observabilidade, contrato e small releases. Palavras-chave: fluxo crítico, auth, sos, billing, risco, regressão, observabilidade reforçada."
---

# Fluxo Crítico

Use este prompt quando a mudança tocar um fluxo sensível e o custo de regressão for alto.

## Objetivo

Orientar a análise e a implementação com foco em risco, cobertura e observabilidade reforçada.

## O que você deve fazer

1. Identificar por que o fluxo é crítico.
2. Mapear os principais riscos funcionais e operacionais.
3. Consultar docs e instructions relevantes.
4. Sugerir o melhor agent e as skills de apoio.
5. Propor uma sequência pequena e segura de próximos passos.

## Formato de resposta

### Leitura inicial

- Fluxo crítico afetado
- Riscos principais
- Camadas afetadas

### Guardrails

- TDD
- Observabilidade
- Contrato
- Small releases

### Melhor caminho

- Agent recomendado
- Skills de apoio

### Próximos passos

Liste os próximos passos mais seguros.

## Restrições

- Não minimizar risco de regressão.
- Não agrupar mudanças grandes em um único slice.
