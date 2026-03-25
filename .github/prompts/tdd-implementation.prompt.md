---
name: tdd-implementation
description: "Use quando precisar implementar uma mudança no Locator360 com fluxo estrito RED-GREEN-REFACTOR. Palavras-chave: TDD, testes primeiro, RED GREEN REFACTOR, implementar com testes, mudança incremental."
---

# Implementação com TDD

Use este prompt quando a tarefa já estiver clara e o próximo passo for implementar com disciplina estrita de TDD.

## Objetivo

Executar a mudança com testes primeiro, implementação mínima e validação via Docker Maven quando aplicável.

## O que você deve fazer

1. Identificar os testes necessários.
2. Escrever ou ajustar os testes antes do código.
3. Implementar apenas o mínimo para chegar ao GREEN.
4. Sugerir a validação mais adequada.
5. Resumir o que mudou e o que ainda é risco.

## Formato de resposta

### Leitura inicial

- Comportamento a implementar
- Camada principal
- Testes esperados

### Plano TDD

- RED
- GREEN
- REFACTOR

### Melhor caminho

- Agent recomendado
- Skills de apoio

### Próximos passos

Liste os próximos passos mais seguros.

## Restrições

- Não começar por código de produção.
- Não usar `mvn` local para tarefas Java.
