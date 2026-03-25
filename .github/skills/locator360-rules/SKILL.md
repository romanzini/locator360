---
name: locator360-rules
description: "Use quando precisar das regras principais de trabalho do Locator360 em um único lugar. Palavras-chave: project rules, locator360 workflow, TDD rules, Docker Maven, observability rules, small releases, Vexa rules."
---

# Locator360 Rules

Esta skill condensa as regras do projeto que são fáceis de violar no trabalho do dia a dia.

## Sempre carregue estas referências quando forem relevantes

- commands.md
- Docs/development-workflow.md
- .github/instructions/vexa-architecture.instructions.md
- .github/instructions/git-commits.instructions.md
- .github/instructions/testing.instructions.md
- .github/instructions/observability.instructions.md

## Regras operacionais

### TDD

- Testes primeiro.
- Priorize testes unitários por padrão.
- Siga RED -> GREEN -> REFACTOR.

### Build e run

- Use Docker Maven para test, compile e spring-boot:run.
- Trate commands.md como fonte de verdade.

### Arquitetura

- Preserve api -> core <- infrastructure.
- Mantenha o domain livre de preocupações de framework.
- Use ports como contratos.
- Mantenha DTOs e JPA entities isolados em suas camadas.

### Observabilidade

- @Slf4j em classes de produção.
- debug na entrada, info no sucesso de negócio, error com exceção no ponto de tratamento.
- Adicione métricas de negócio em operações críticas.

### Modelo de entrega

- Uma responsabilidade por commit.
- Prefira small releases a mudanças amplas.
- Use conventional commits com o scope do domínio.

## Lembretes rápidos

- ModelMapper deve ser usado diretamente nos services, salvo configuração especial realmente necessária.
- UUID é o tipo padrão de identificador.
- Não use mvn local para test, compile ou run em Java.

## Prompt relacionado

- Use `.github/prompts/locator360-onboarding.prompt.md` quando quiser carregar este conjunto de regras como parte da entrada inicial da tarefa.
