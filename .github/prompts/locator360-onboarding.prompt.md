---
name: locator360-onboarding
description: "Use quando iniciar uma nova tarefa no Locator360 e precisar carregar rapidamente contexto, regras, docs principais e o fluxo recomendado com agents e skills. Palavras-chave: onboarding, iniciar tarefa, contexto inicial, preparação do projeto, primeiro prompt."
---

# Onboarding Locator360

Use este prompt no início de uma nova tarefa para alinhar rapidamente o trabalho com as regras e a estrutura do projeto.

## Objetivo

Antes de propor implementação, faça uma inicialização orientada do contexto e devolva um plano enxuto para começar com segurança.

## O que você deve fazer

1. Identificar o domínio de negócio principal da tarefa.
2. Consultar as docs e instructions mais relevantes.
3. Resumir as regras obrigatórias que impactam a tarefa.
4. Sugerir o melhor agent ou skill para seguir a partir dali.
5. Propor os próximos passos mínimos.

## Referências prioritárias

- commands.md
- Docs/development-workflow.md
- Docs/copilot-agents-and-skills-guide.md
- Docs/especificacao-funcional.md
- Docs/database-model.md
- Docs/openapi.yaml
- .github/instructions/vexa-architecture.instructions.md
- .github/instructions/testing.instructions.md
- .github/instructions/observability.instructions.md
- .github/instructions/git-commits.instructions.md

## Formato de resposta

Responda com estas seções:

### Leitura inicial

- Domínio principal
- Tipo de tarefa
- Camadas provavelmente afetadas

### Regras que importam agora

- TDD
- Docker Maven
- Vexa
- Observabilidade
- Small releases

### Melhor caminho

- Agent recomendado
- Skills de apoio
- Justificativa curta

### Próximos passos

Liste os 3 a 5 próximos passos mais úteis e mais seguros.

## Restrições

- Não pule direto para implementação se o escopo ainda estiver difuso.
- Não recomende `mvn` local para tarefas Java.
- Não proponha mudanças grandes sem antes sugerir divisão em slices quando necessário.