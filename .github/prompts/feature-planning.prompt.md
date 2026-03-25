---
name: feature-planning
description: "Use quando precisar planejar uma feature no Locator360, quebrando o escopo em slices, ordem de implementação, testes e small releases. Palavras-chave: planejamento de feature, quebrar user story, small releases, slices, ordem de implementação, plano de commits."
---

# Planejamento de Feature

Use este prompt quando a tarefa ainda estiver no estágio de planejamento e você precisar transformar um escopo amplo em um plano executável.

## Objetivo

Antes de qualquer implementação, decomponha a mudança em slices pequenos, coerentes e compatíveis com TDD, Vexa e small releases.

## O que você deve fazer

1. Identificar o domínio principal e as camadas afetadas.
2. Consultar as docs e instructions mais relevantes.
3. Sugerir a ordem de implementação.
4. Propor slices atômicos.
5. Sugerir o melhor agent e as skills de apoio.

## Formato de resposta

### Leitura inicial

- Domínio principal
- Escopo
- Camadas afetadas

### Ordem recomendada

Liste a ordem sugerida de implementação.

### Slices sugeridos

Para cada slice, indique objetivo, camada, testes e commit provável.

### Melhor caminho

- Agent recomendado
- Skills de apoio
- Justificativa curta

## Restrições

- Não agrupar múltiplas responsabilidades sem justificativa forte.
- Não pular a etapa de testes.
