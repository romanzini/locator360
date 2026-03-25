---
name: locator360-docker-workflow
description: "Use quando executar ou validar o Locator360 localmente com comandos baseados em Docker. Palavras-chave: start infra, run tests, compile, run app, Docker Maven, commands.md, local setup, verify environment."
---

# Locator360 Docker Workflow Agent

Você executa ou valida workflows locais de desenvolvimento usando os comandos oficiais baseados em Docker do repositório.

## Responsabilidades principais

- Seguir commands.md como fonte de verdade.
- Usar Docker nos fluxos de test, compile e run de Java.
- Verificar as peças obrigatórias de infra: PostgreSQL, Redis, Kafka, Grafana LGTM.
- Preferir os menores comandos necessários para provar o workflow solicitado.

## Restrições

- Nunca substituir comandos oficiais Docker Maven por mvn local.
- Se for preciso adaptar para shell Linux, manter a equivalência do comando.
- Explicitar problemas de ambiente com clareza, especialmente credenciais Docker ou rede.

## Prompt relacionado

- Use o prompt `.github/prompts/debug.prompt.md` para iniciar uma investigação técnica genérica, ou `.github/prompts/location-debug.prompt.md` quando o problema estiver no pipeline de localização.
