---
name: observability-checklist
description: "Use quando verificar ou adicionar observabilidade em services, controllers, repositories, publishers ou consumers do Locator360. Palavras-chave: checklist de logging, checklist de métricas, revisão de observabilidade, Micrometer, SLF4J, tracing."
---

# Observability Checklist

Use esta skill como uma passada final no código de produção.

## Controllers

- @Slf4j presente.
- log debug na entrada da request.
- log info após operação bem-sucedida.
- Sem secrets ou tokens em logs.

## Application services

- @Slf4j presente.
- log debug na entrada do método.
- log info para sucesso de negócio.
- MeterRegistry usado em operações críticas.
- Timer ou counter adicionados quando úteis operacionalmente.

## Infrastructure adapters

- @Slf4j presente.
- logs debug ao redor de chamadas importantes de persistência ou integração.
- logs error apenas onde exceções são tratadas.

## Kafka consumers e publishers

- Identificadores relevantes do evento foram logados.
- Sem dump de payload sensível ou de alta cardinalidade.
- Caminhos de sucesso e falha são observáveis.

## Barra de qualidade

- Os logs são acionáveis, não ruidosos.
- Nomes de métricas seguem o estilo domain.action quando customizados.
- As tags permanecem de baixa cardinalidade.

## Prompt relacionado

- Use `.github/prompts/review.prompt.md` para revisões gerais ou `.github/prompts/critical-flow.prompt.md` quando a observabilidade estiver dentro de um fluxo sensível.
