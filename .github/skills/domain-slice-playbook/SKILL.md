---
name: domain-slice-playbook
description: "Use quando implementar uma nova feature do Locator360 por domínio e camada. Palavras-chave: novo caso de uso, implementar domínio, slice de feature, feature de auth, feature de circle, feature de location, plano por camada."
---

# Domain Slice Playbook

Use este workflow ao implementar uma feature no Locator360.

## Passo 1: esclarecer o slice

- Identifique o domínio de negócio.
- Leia as docs de negócio relevantes.
- Confirme se uma migration é necessária.
- Confirme se o contrato já existe no OpenAPI ou nas docs.

## Passo 2: construir de dentro para fora

Sequência recomendada:

1. Migration, se necessária.
2. Entidades de domínio, enums e value objects.
3. Testes de domínio.
4. Ports e DTOs.
5. Testes de application service.
6. Implementação de application service.
7. Testes de adapters de infrastructure.
8. Implementação de infrastructure.
9. Testes de API.
10. Implementação de API.

## Passo 3: preservar small releases

Para cada slice, defina:

- uma camada ou uma preocupação altamente coesa
- testes que a comprovem
- uma mensagem de conventional commit

## Passo 4: verificar conclusão

- Os testes passam com Docker Maven.
- Logs e métricas estão presentes onde exigidos.
- Não há vazamento entre camadas.
- OpenAPI e docs estão atualizados quando o contrato muda.

## Prompt relacionado

- Use `.github/prompts/feature-planning.prompt.md` quando quiser começar pela intenção de planejamento e depois aplicar este playbook para refinar a execução por camadas.
