---
name: docs-to-code
description: "Use quando traduzir documentação do Locator360 em mudanças de código. Palavras-chave: implementar a partir das docs, docs para código, derivar do modelo de banco, derivar do OpenAPI, derivar da especificação de negócio, comparar docs e código."
---

# Docs to Code

Use esta skill quando a solicitação começar pela documentação, e não pelo código existente.

## Fontes principais

- Docs/especificacao-funcional.md para regras de negócio
- Docs/database-model.md para tabelas e constraints
- Docs/openapi.yaml para contratos de request e response
- Docs/api-usage.md para expectativas de fluxo

## Workflow

1. Extraia da documentação a regra ou o contrato.
2. Mapeie isso para o domínio e a camada de destino.
3. Identifique se o código já existe.
4. Liste divergências entre docs e código.
5. Implemente apenas o slice mínimo necessário.

## Saída esperada

- O que a documentação diz
- O que o código faz hoje
- Lacunas a fechar
- Ordem recomendada de implementação

## Prompt relacionado

- Use `.github/prompts/feature-planning.prompt.md` quando a documentação for o ponto de partida do planejamento, ou `.github/prompts/tdd-implementation.prompt.md` quando a regra já estiver pronta para implementação.
