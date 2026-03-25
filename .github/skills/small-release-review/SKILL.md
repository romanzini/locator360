---
name: small-release-review
description: "Use quando verificar se uma mudança do Locator360 está grande demais ou mistura responsabilidades. Palavras-chave: small release, dividir commit, dividir PR, revisar escopo, mudança atômica, escopo de conventional commit."
---

# Small Release Review

Use esta skill para manter as mudanças revisáveis e reversíveis.

## Perguntas de revisão

- A mudança tem uma única responsabilidade?
- Um único commit tenta cobrir múltiplas camadas sem um bom motivo?
- O título precisaria de um "e" para descrever o trabalho?
- Há arquivos sem relação no diff?
- Os testes foram agrupados de forma sensata com o código que protegem?

## Diretrizes típicas de divisão

- Migration separada de application ou API, salvo quando o slice for minúsculo e inseparável.
- Domain separado do trabalho de controller.
- Testes podem ser um slice próprio no RED estrito, ou agrupados apenas quando a mudança for muito pequena.
- Trabalho de docs ou chore não deve ficar escondido dentro de commit de feature.

## Saída esperada

- Avaliação do escopo atual
- Se deve ser dividido
- Limites sugeridos para divisão
- Sugestões de conventional commits

## Prompt relacionado

- Use `.github/prompts/feature-planning.prompt.md` quando a necessidade principal for quebrar o escopo antes de implementar, ou `.github/prompts/review.prompt.md` quando a divisão surgir durante revisão.
