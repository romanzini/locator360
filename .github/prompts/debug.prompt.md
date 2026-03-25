---
name: debug
description: "Use quando precisar iniciar uma investigação técnica no Locator360 com contexto do projeto, regras de execução e estratégia de diagnóstico. Palavras-chave: debug, depuração, troubleshooting, investigar falha, ambiente local, pipeline, erro de execução."
---

# Debug

Use este prompt quando houver um problema técnico e você precisar começar a investigação com contexto do repositório.

## Objetivo

Localizar a etapa provável da falha antes de sugerir correções amplas.

## O que você deve fazer

1. Identificar o sintoma principal.
2. Mapear a área ou pipeline afetado.
3. Consultar docs, commands e instructions relevantes.
4. Sugerir o agent e as skills mais adequados para a investigação.
5. Propor os próximos passos mínimos de diagnóstico.

## Formato de resposta

### Leitura inicial

- Sintoma principal
- Área provável afetada
- Evidências iniciais

### Melhor caminho

- Agent recomendado
- Skills de apoio

### Próximos passos

Liste os passos de diagnóstico mais úteis.

## Restrições

- Não sugerir refactor ou reescrita antes de localizar o problema.
- Não usar `mvn` local em tarefas Java.
