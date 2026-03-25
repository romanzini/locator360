---
name: rest-endpoint-checklist
description: "Use quando criar ou revisar um endpoint REST no Locator360. Palavras-chave: checklist de endpoint, checklist de controller, contrato HTTP, código de status, validação de request, atualização de OpenAPI, teste de controller."
---

# REST Endpoint Checklist

Use este checklist antes de considerar uma mudança REST como concluída.

## Contrato

- Rota e verbo batem com Docs/openapi.yaml e Docs/api-usage.md.
- O status code está correto para a operação.
- O DTO de request corresponde ao contrato esperado.
- O DTO de response corresponde ao contrato esperado.
- Compatibilidade retroativa foi considerada.

## Estrutura do controller

- O controller não contém lógica de negócio.
- A validação de request está presente.
- O controller delega para um port in ou use case.
- O padrão de controller baseado em interface é preservado se já existir no módulo.

## Observabilidade

- @Slf4j presente.
- log debug na entrada.
- log info na conclusão com sucesso.
- Nenhum valor sensível em logs.

## Testes

- Os testes unitários do controller cobrem status code.
- Os testes unitários do controller cobrem o formato do payload quando relevante.
- Os testes unitários do controller cobrem falhas de validação.

## Docs

- O OpenAPI foi atualizado se o contrato mudou.
- As docs do repositório que expõem o endpoint permanecem alinhadas.

## Prompt relacionado

- Use `.github/prompts/review.prompt.md` para iniciar uma revisão de endpoint, ou `.github/prompts/tdd-implementation.prompt.md` quando estiver no momento de implementar o contrato.
