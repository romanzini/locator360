# Guia de Agents e Skills do Copilot

Este guia explica quando usar cada agent customizado e cada skill customizada criada para o Locator360.

O objetivo é simples: reduzir improviso, manter as mudanças alinhadas com as regras do repositório e tornar o fluxo com IA repetível para o time.

## 1. Como pensar em agents e skills

Use um agent quando a tarefa for um fluxo maior, com múltiplas etapas, decisões, critérios de revisão ou necessidade de isolamento de contexto.

Use uma skill quando você precisar de um playbook reutilizável, checklist ou conjunto focado de conhecimento do projeto para aplicar dentro de uma tarefa maior.

Na prática:

- Agents são melhores para planejamento, implementação, revisão e fluxos operacionais.
- Skills são melhores para checklists, regras do repositório e playbooks especializados.

## 2. Roteamento rápido

Use esta seção quando você só quiser escolher a melhor opção rapidamente.

| Se você precisa... | Use isto |
|---|---|
| Quebrar uma user story em slices e commits atômicos | `locator360-feature-slicer` |
| Implementar uma mudança com testes primeiro | `locator360-tdd-implementer` |
| Revisar problemas de arquitetura e acoplamento | `locator360-vexa-guard` |
| Implementar ou revisar um endpoint contra o contrato | `locator360-api-contract` |
| Adicionar ou revisar logs, métricas e tracing | `locator360-observability` |
| Executar ou validar workflows locais com Docker | `locator360-docker-workflow` |
| Revisar um diff ou PR em busca de riscos e testes faltantes | `locator360-pr-review` |
| Carregar rapidamente as regras principais do projeto | `locator360-rules` |
| Seguir a sequência padrão por domínio e camada | `domain-slice-playbook` |
| Validar um checklist de endpoint REST | `rest-endpoint-checklist` |
| Validar cobertura de observabilidade | `observability-checklist` |
| Traduzir documentação em implementação | `docs-to-code` |
| Decidir se a mudança precisa ser dividida | `small-release-review` |
| Depurar ingestão de localização ou fluxo de eventos | `location-event-debug` |

## 3. Tabela consolidada por cenário

Use esta tabela quando quiser decidir rapidamente a combinação ideal entre `agent`, `skill` e `prompt` compartilhado.

| Cenário | Agent principal | Skill de apoio | Prompt recomendado |
|---|---|---|---|
| Iniciar tarefa genérica | `locator360-feature-slicer` | `locator360-rules` | `locator360-onboarding` |
| Nova feature por domínio | `locator360-feature-slicer` | `domain-slice-playbook` | `feature-planning` |
| Implementação em TDD | `locator360-tdd-implementer` | `locator360-rules` | `tdd-implementation` |
| Fluxo crítico como auth, sos ou billing | `locator360-pr-review` | `small-release-review` | `critical-flow` |
| Endpoint REST novo ou alterado | `locator360-api-contract` | `rest-endpoint-checklist` | `locator360-onboarding` |
| Revisão arquitetural | `locator360-vexa-guard` | `locator360-rules` | `review` |
| Revisão final de PR | `locator360-pr-review` | `small-release-review` | `review` |
| Debug técnico genérico | `locator360-docker-workflow` | `locator360-rules` | `debug` |
| Debug de pipeline de location | `locator360-docker-workflow` | `location-event-debug` | `location-debug` |

## 4. Agents

### 4.1 `locator360-feature-slicer`

Arquivo: [.github/agents/locator360-feature-slicer.agent.md](/home/romanzini/repos/locator360/.github/agents/locator360-feature-slicer.agent.md)

Use quando:

- você tem uma nova user story
- você precisa planejar a implementação por camada
- você quer um plano de commits ou PRs que respeite small releases
- a solicitação está ampla demais e precisa ser quebrada com segurança

Melhor para:

- planejamento de feature
- decomposição de bugfix
- definição da ordem de implementação
- definição dos slices de teste

Prompts típicos:

- "Quebre a US-021 em small releases para o domínio auth."
- "Divida esta feature de círculos em slices TDD e commits."
- "Planeje a ordem de implementação para este endpoint e a mudança de repositório."

Saída esperada:

- domínio e escopo
- camadas afetadas
- slices ordenados
- arquivos prováveis
- orientação de testes primeiro
- sugestões de conventional commits

Não use quando:

- você só precisa de um checklist rápido
- a tarefa já está reduzida a um arquivo pequeno ou correção óbvia

### 4.2 `locator360-tdd-implementer`

Arquivo: [.github/agents/locator360-tdd-implementer.agent.md](/home/romanzini/repos/locator360/.github/agents/locator360-tdd-implementer.agent.md)

Use quando:

- você quer que a IA implemente sob fluxo estrito RED -> GREEN -> REFACTOR
- você quer testes escritos antes do código de produção
- você quer manter a mudança no escopo de testes unitários por padrão

Melhor para:

- novos casos de uso
- implementação de services
- mudanças de comportamento no domínio
- mudanças em controllers e adapters que exigem disciplina forte de TDD

Prompts típicos:

- "Implemente este caso de uso de auth com TDD estrito."
- "Adicione os testes primeiro para o service de política de alerta de lugar e depois implemente o mínimo."
- "Siga RED GREEN REFACTOR para esta regra de associação a círculo."

Saída esperada:

- testes criados primeiro
- código mínimo para ficar verde
- validação direcionada com Docker Maven
- resumo do que mudou e do que ainda é arriscado

Não use quando:

- a solicitação for apenas de revisão
- a solicitação for depurar setup de ambiente, e não comportamento de código

### 4.3 `locator360-vexa-guard`

Arquivo: [.github/agents/locator360-vexa-guard.agent.md](/home/romanzini/repos/locator360/.github/agents/locator360-vexa-guard.agent.md)

Use quando:

- você quer uma revisão arquitetural
- você suspeita que um controller tem lógica de negócio
- você quer verificar vazamento entre camadas ou direção de dependência quebrada

Melhor para:

- revisão antes de PR
- revisão de refactor
- validação do uso de ports e adapters
- detecção de vazamento de DTO ou entity entre camadas

Prompts típicos:

- "Revise esta mudança em busca de violações da Vexa."
- "Verifique se este service e este repository quebram as regras hexagonais."
- "Audite este controller para vazamento de lógica de negócio."

Saída esperada:

- findings ordenados por severidade
- explicação de por que o problema viola a Vexa
- direção concreta de correção

Não use quando:

- a principal preocupação for correção do contrato HTTP, e não arquitetura

### 4.4 `locator360-api-contract`

Arquivo: [.github/agents/locator360-api-contract.agent.md](/home/romanzini/repos/locator360/.github/agents/locator360-api-contract.agent.md)

Use quando:

- você está criando ou alterando um endpoint REST
- você precisa comparar o código com OpenAPI ou documentação de uso da API
- você quer validar status code, DTOs, validação e comportamento do controller

Melhor para:

- implementação de endpoint
- revisão de endpoint
- validação de contrato do controller
- checagem de alinhamento de DTOs

Prompts típicos:

- "Implemente este endpoint a partir do OpenAPI."
- "Revise o contrato do location controller contra a documentação."
- "Verifique se este status code e este DTO batem com o contrato esperado."

Saída esperada:

- checklist ou plano de contrato
- divergências entre docs e código
- riscos focados no controller

Não use quando:

- o problema for principalmente de infraestrutura ou modelagem de domínio sem impacto HTTP

### 4.5 `locator360-observability`

Arquivo: [.github/agents/locator360-observability.agent.md](/home/romanzini/repos/locator360/.github/agents/locator360-observability.agent.md)

Use quando:

- você precisa adicionar logs e métricas a uma feature
- você quer uma passada focada só em qualidade de observabilidade
- você suspeita que o código de produção está sem logging obrigatório ou sem instrumentação com `MeterRegistry`

Melhor para:

- passada final antes de revisão
- atualização de observabilidade em services e controllers
- revisão de logging em repositories, publishers e consumers

Prompts típicos:

- "Adicione a observabilidade obrigatória neste caso de uso."
- "Revise estas mudanças em busca de gaps de logging e métricas."
- "Verifique se este consumer Kafka está observável o suficiente."

Saída esperada:

- peças de observabilidade ausentes
- mudanças de código ou itens de checklist
- alertas sobre logs sensíveis ou métricas ruins

Não use quando:

- o problema principal for arquitetura ou formato do contrato HTTP

### 4.6 `locator360-docker-workflow`

Arquivo: [.github/agents/locator360-docker-workflow.agent.md](/home/romanzini/repos/locator360/.github/agents/locator360-docker-workflow.agent.md)

Use quando:

- você precisa rodar ou validar o ambiente local
- você precisa executar testes, compilação ou subir a aplicação com os comandos oficiais
- você quer ajuda com o fluxo Java restrito a Docker neste repositório

Melhor para:

- validação de startup
- execução de testes com Docker Maven
- checagem de compilação
- diagnóstico de infra

Prompts típicos:

- "Rode o workflow oficial de testes deste projeto."
- "Valide o setup local do Locator360 usando apenas comandos Docker."
- "Suba a infra e explique o que está faltando se falhar."

Saída esperada:

- comandos alinhados a `commands.md`
- diagnóstico de ambiente
- direção de correção focada em Docker

Não use quando:

- a tarefa for puramente de revisão de código ou planejamento

### 4.7 `locator360-pr-review`

Arquivo: [.github/agents/locator360-pr-review.agent.md](/home/romanzini/repos/locator360/.github/agents/locator360-pr-review.agent.md)

Use quando:

- você quer uma code review do diff atual ou de um PR
- você precisa que bugs, riscos, testes faltantes e violações de workflow sejam apontados com clareza
- você quer findings no padrão de exigência deste repositório

Melhor para:

- revisão antes do merge
- auto-revisão antes de abrir PR
- checagem de small release e cobertura faltante

Prompts típicos:

- "Revise as mudanças atuais."
- "Faça um PR review focado em bugs e testes faltantes."
- "Verifique se esta mudança viola as regras de small release."

Saída esperada:

- findings primeiro
- ordem por severidade
- direção concreta de correção
- riscos residuais se nada bloqueante for encontrado

Não use quando:

- você precisa de implementação, não de revisão

## 5. Skills

### 5.1 `locator360-rules`

Arquivo: [.github/skills/locator360-rules/SKILL.md](/home/romanzini/repos/locator360/.github/skills/locator360-rules/SKILL.md)

Use quando:

- você quer carregar rapidamente as regras principais do projeto
- você está em dúvida sobre testes, arquitetura, observabilidade ou regras de entrega
- você precisa de um alinhamento rápido e específico do repositório antes de trabalhar

Melhor para:

- início de tarefa
- checagem de alinhamento da IA com as convenções do repositório
- redução de desvio entre tarefas diferentes

Prompts típicos:

- "Aplique as regras do Locator360 a esta tarefa."
- "Quais regras do repositório importam antes de implementar esta feature?"

### 5.2 `domain-slice-playbook`

Arquivo: [.github/skills/domain-slice-playbook/SKILL.md](/home/romanzini/repos/locator360/.github/skills/domain-slice-playbook/SKILL.md)

Use quando:

- você quer a sequência padrão para implementar uma feature por camada
- você precisa de um playbook repetível para auth, circle, location, place ou qualquer outro domínio

Melhor para:

- início de feature
- sequenciamento de implementação
- evitar erro de ordem entre camadas

Prompts típicos:

- "Use o domain slice playbook para esta feature de notificações."
- "Qual é a sequência de dentro para fora para implementar este fluxo de auth?"

### 5.3 `rest-endpoint-checklist`

Arquivo: [.github/skills/rest-endpoint-checklist/SKILL.md](/home/romanzini/repos/locator360/.github/skills/rest-endpoint-checklist/SKILL.md)

Use quando:

- você precisa de um checklist focado em endpoint
- você quer confirmar a qualidade do controller sem invocar um agent maior de revisão

Melhor para:

- passada final de controller
- checagem rápida de contrato
- auto-revisão focada em endpoint

Prompts típicos:

- "Rode o checklist de endpoint REST neste controller."
- "O que falta para este endpoint ser considerado pronto?"

### 5.4 `observability-checklist`

Arquivo: [.github/skills/observability-checklist/SKILL.md](/home/romanzini/repos/locator360/.github/skills/observability-checklist/SKILL.md)

Use quando:

- você precisa de uma passada curta de observabilidade
- você quer um checklist final após implementar service, controller, repository, consumer ou publisher

Melhor para:

- revisão de linha de chegada
- validação direcionada de logs e métricas

Prompts típicos:

- "Aplique o checklist de observabilidade neste service."
- "Quais logs e métricas ainda estão faltando aqui?"

### 5.5 `docs-to-code`

Arquivo: [.github/skills/docs-to-code/SKILL.md](/home/romanzini/repos/locator360/.github/skills/docs-to-code/SKILL.md)

Use quando:

- a solicitação começa pela documentação, não pelo código
- você precisa comparar especificação funcional, modelo de banco ou OpenAPI com o estado atual
- você quer extrair orientação de implementação a partir da documentação do projeto

Melhor para:

- itens de backlog definidos em docs
- análise de divergência entre código e documentação
- derivação de migrations, DTOs ou entidades a partir da documentação

Prompts típicos:

- "Implemente isto a partir do modelo de banco."
- "Compare a documentação e o código deste endpoint."
- "Derive o próximo slice de implementação a partir do OpenAPI."

### 5.6 `small-release-review`

Arquivo: [.github/skills/small-release-review/SKILL.md](/home/romanzini/repos/locator360/.github/skills/small-release-review/SKILL.md)

Use quando:

- você quer decidir se uma mudança está grande demais
- você precisa dividir um diff em commits ou PRs menores
- você suspeita que múltiplas responsabilidades foram misturadas

Melhor para:

- revisão antes de commit
- revisão antes de PR
- planejamento de divisão de trabalho entre camadas

Prompts típicos:

- "Esta mudança está grande demais para um PR só?"
- "Como devo dividir isto em commits atômicos?"

### 5.7 `location-event-debug`

Arquivo: [.github/skills/location-event-debug/SKILL.md](/home/romanzini/repos/locator360/.github/skills/location-event-debug/SKILL.md)

Use quando:

- o endpoint de stream de localização está falhando
- o fluxo Kafka a partir da ingestão de localização está suspeito
- geofence ou drive detection não estão se comportando como esperado
- você precisa de um playbook de depuração para o pipeline assíncrono de localização

Melhor para:

- problemas de ingestão
- depuração de fluxo de eventos
- diagnóstico conjunto de infra e aplicação para features de localização

Prompts típicos:

- "Depure o pipeline de stream de localização."
- "Investigue por que os eventos de localização não estão chegando aos consumers downstream."
- "Use o playbook de location event debug para esta falha."

## 6. Exemplos por domínio

Use estes exemplos como ponto de partida. Eles não substituem contexto de negócio, mas ajudam o time a acionar o agent ou a skill certa com menos ambiguidade.

### 6.1 Auth

- "Use `locator360-feature-slicer` para quebrar a US de registro com e-mail em small releases no domínio auth."
- "Use `locator360-tdd-implementer` para implementar o caso de uso de login com telefone em TDD estrito."
- "Use `docs-to-code` para comparar o fluxo de recuperação de senha nas docs com o código atual."
- "Use `locator360-api-contract` para revisar se o endpoint de refresh token está aderente ao contrato."

### 6.2 Circle

- "Use `domain-slice-playbook` para planejar a implementação da remoção de membro em circle."
- "Use `locator360-tdd-implementer` para criar os testes primeiro da transferência de admin."
- "Use `small-release-review` para dividir esta feature de convites em commits atômicos."
- "Use `locator360-vexa-guard` para revisar se o controller de circle está vazando regra de negócio."

### 6.3 Location

- "Use `locator360-api-contract` para validar o contrato do endpoint de stream de localização."
- "Use `location-event-debug` para investigar por que os eventos não chegam ao Kafka."
- "Use `locator360-observability` para revisar logs e métricas do fluxo de ingestão."
- "Use `locator360-docker-workflow` para validar a infra necessária ao pipeline de localização."

### 6.4 Place

- "Use `docs-to-code` para derivar a implementação de places e geofence a partir do modelo de dados e OpenAPI."
- "Use `locator360-feature-slicer` para separar em slices a criação de places, policies e alert targets."
- "Use `rest-endpoint-checklist` para revisar o endpoint de place antes do PR."
- "Use `observability-checklist` para validar a observabilidade do service de geofence."

### 6.5 Drive

- "Use `domain-slice-playbook` para organizar a implementação da detecção de viagens por camada."
- "Use `locator360-observability` para revisar métricas e logs do cálculo de safety score."
- "Use `small-release-review` para checar se a mudança no detector de drive está grande demais."

### 6.6 SOS

- "Use `locator360-feature-slicer` para quebrar a feature de SOS em slices de domínio, publisher e API."
- "Use `locator360-tdd-implementer` para implementar o acionamento de SOS com testes primeiro."
- "Use `locator360-pr-review` para revisar riscos de regressão nesse fluxo crítico."

### 6.7 Notification

- "Use `docs-to-code` para comparar a estratégia de notificações descrita nas docs com os adapters atuais."
- "Use `locator360-observability` para revisar publishers e consumers de notificações."
- "Use `locator360-vexa-guard` para garantir que integrações externas continuem atrás de ports out."

### 6.8 Plan e Billing

- "Use `locator360-feature-slicer` para dividir a feature de subscriptions em small releases."
- "Use `locator360-api-contract` para revisar endpoints de plans e subscriptions."
- "Use `small-release-review` para separar mudanças de billing de ajustes de infraestrutura."

### 6.9 Admin

- "Use `locator360-vexa-guard` para revisar se a API de admin não está vazando detalhes de persistence."
- "Use `locator360-pr-review` para revisar riscos de autorização e testes faltantes na área admin."
- "Use `rest-endpoint-checklist` para fechar a revisão do endpoint antes de abrir PR."

### 6.10 Place

- "Use `feature-planning` para iniciar esta tarefa de geofence com um plano por slices."
- "Use `locator360-api-contract` para validar o endpoint de place contra o OpenAPI."
- "Use `docs-to-code` para comparar policies e alert targets com o modelo de dados."

### 6.11 Notification

- "Use `feature-planning` para iniciar esta tarefa de notification com contexto suficiente e plano de execução."
- "Use `locator360-observability` para revisar logging e métricas do dispatch."
- "Use `locator360-vexa-guard` para validar adapters externos de notificação atrás de ports out."

### 6.12 SOS

- "Use `critical-flow` para iniciar esta tarefa crítica de SOS com foco em risco e observabilidade."
- "Use `locator360-feature-slicer` para dividir a feature de SOS em slices pequenos."
- "Use `locator360-pr-review` para revisar regressões potenciais nesse fluxo crítico."

## 7. Combinações recomendadas

Essas combinações normalmente funcionam melhor do que usar uma customização isolada.

### Nova feature a partir de uma user story

1. `locator360-feature-slicer`
2. `domain-slice-playbook`
3. `locator360-tdd-implementer`
4. `observability-checklist`
5. `small-release-review`

### Endpoint REST novo ou alterado

1. `docs-to-code`
2. `locator360-api-contract`
3. `rest-endpoint-checklist`
4. `locator360-tdd-implementer`

### Refactor sensível à arquitetura

1. `locator360-vexa-guard`
2. `locator360-tdd-implementer`
3. `small-release-review`

### Revisão final antes do PR

1. `locator360-pr-review`
2. `observability-checklist`
3. `small-release-review`

### Problema de ambiente local ou pipeline

1. `locator360-docker-workflow`
2. `location-event-debug` quando o problema estiver no fluxo de localização

## 8. Prompts prontos por tipo de tarefa

Use estes exemplos quando a necessidade estiver mais ligada ao tipo de trabalho do que ao domínio.

### 8.1 Planejamento

- "Use o prompt `feature-planning` para quebrar esta user story em small releases com ordem de implementação e commits sugeridos."
- "Use `locator360-feature-slicer` e `domain-slice-playbook` para montar a sequência por camadas desta feature."
- "Use `small-release-review` para dizer se este escopo está grande demais para um único PR."

### 8.2 Implementação

- "Use o prompt `tdd-implementation` para implementar esta mudança com testes primeiro e validação via Docker Maven."
- "Use `docs-to-code` para transformar esta regra documentada em uma implementação mínima."
- "Use `locator360-api-contract` para implementar este endpoint sem desviar do contrato."

### 8.3 Revisão

- "Use o prompt `review` para iniciar a revisão do diff atual em busca de bugs, regressões e testes faltantes."
- "Use `locator360-vexa-guard` para revisar esta mudança sob a ótica da arquitetura Vexa."
- "Use `rest-endpoint-checklist` para uma revisão rápida deste controller antes do PR."

### 8.4 Observabilidade

- "Use `locator360-observability` para revisar logs e métricas desta feature antes de considerar pronta."
- "Use `observability-checklist` para validar o mínimo obrigatório de observabilidade neste service."

### 8.5 Debugging

- "Use o prompt `debug` para validar o ambiente local e apontar falhas de execução."
- "Use `location-debug` ou `location-event-debug` para investigar problemas no pipeline de localização."
- "Use `docs-to-code` para comparar o comportamento atual com o que a documentação esperava."

### 8.6 Onboarding

- "Use `locator360-rules` para resumir as regras obrigatórias do projeto antes de começar a tarefa."
- "Use o prompt compartilhado de onboarding do Locator360 para inicializar contexto, regras e próximos passos."

### 8.7 Fluxo crítico

- "Use o prompt `critical-flow` para iniciar esta mudança em auth, sos ou billing com foco em risco e regressão."
- "Use `locator360-pr-review` para priorizar os riscos mais relevantes desse fluxo crítico."
- "Use `observability-checklist` para reforçar a cobertura operacional da mudança."

## 9. Prompts por maturidade da tarefa

Use esta seção quando a dúvida não for sobre domínio nem sobre tipo exato de ação, mas sobre o grau de definição da tarefa.

### 9.1 Escopo difuso

Use quando a tarefa ainda está vaga, mistura várias camadas ou não está claro por onde começar.

- Prompt principal: `locator360-onboarding`
- Prompt alternativo: `feature-planning`
- Melhor combinação: `locator360-rules` + `locator360-feature-slicer`

### 9.2 Escopo claro, implementação pronta para começar

Use quando a regra já está entendida e o próximo passo natural é implementar.

- Prompt principal: `tdd-implementation`
- Prompt alternativo: `locator360-onboarding`
- Melhor combinação: `locator360-tdd-implementer` + `docs-to-code` quando houver contrato ou regra documentada

### 9.3 Mudança em revisão ou fechamento

Use quando já existe diff, PR, endpoint implementado ou necessidade de validação final.

- Prompt principal: `review`
- Prompt alternativo: `critical-flow` se o fluxo for sensível
- Melhor combinação: `locator360-pr-review` + `rest-endpoint-checklist` ou `observability-checklist`

### 9.4 Falha técnica ou comportamento inesperado

Use quando o objetivo principal for diagnosticar um erro, falha de execução ou desvio entre esperado e observado.

- Prompt principal: `debug`
- Prompt alternativo: `location-debug` quando o problema estiver no pipeline de localização
- Melhor combinação: `locator360-docker-workflow` + `location-event-debug` quando houver Kafka, consumers ou ingestão assíncrona envolvidos

### 9.5 Fluxo crítico ou de alto risco

Use quando a mudança tocar auth, sos, billing ou outro caminho cujo custo de regressão seja alto.

- Prompt principal: `critical-flow`
- Prompt alternativo: `review`
- Melhor combinação: `locator360-pr-review` + `small-release-review` + `observability-checklist`

## 10. Regras de decisão

Use as regras abaixo quando houver dúvida.

- Se a tarefa for ampla e multi-etapas, comece com um agent.
- Se a tarefa for estreita e em formato de checklist, comece com uma skill.
- Se a tarefa começar pela documentação, use `docs-to-code` cedo.
- Se a tarefa tocar comportamento HTTP, traga `locator360-api-contract` ou `rest-endpoint-checklist`.
- Se a tarefa tocar código de produção, finalize com `observability-checklist`.
- Se a tarefa parecer ampla demais, use `locator360-feature-slicer` ou `small-release-review` antes de implementar.

## 11. Anti-patterns

Evite estes padrões:

- usar `locator360-tdd-implementer` só depois de o código de produção já estar quase todo escrito
- usar `locator360-pr-review` como substituto para planejar uma feature ampla
- usar `locator360-api-contract` para um problema que na verdade é de modelagem de domínio ou wiring de infraestrutura
- pular `small-release-review` em mudanças que tocam várias camadas ao mesmo tempo
- tratar `locator360-rules` como opcional em tarefas que afetam múltiplas camadas

## 12. Fluxo sugerido para o time

Para a maior parte do trabalho de feature, este é um padrão sólido:

1. Comece com `locator360-rules`.
2. Quebre o trabalho com `locator360-feature-slicer`.
3. Use `docs-to-code` se a fonte de verdade estiver na documentação.
4. Implemente com `locator360-tdd-implementer`.
5. Valide endpoint ou arquitetura com `locator360-api-contract` ou `locator360-vexa-guard`.
6. Finalize com `observability-checklist`.
7. Revise o escopo com `small-release-review`.
8. Faça uma passada final com `locator360-pr-review`.

Essa sequência mantém a IA alinhada com as restrições mais fortes deste repositório: TDD, camadas Vexa, observabilidade e small releases.

## 13. Estratégia de prompts compartilhados

O projeto adota uma estratégia de prompts mais genéricos por intenção de trabalho, em vez de uma coleção grande de prompts por domínio.

Prompts principais:

- [.github/prompts/locator360-onboarding.prompt.md](/home/romanzini/repos/locator360/.github/prompts/locator360-onboarding.prompt.md)
- [.github/prompts/feature-planning.prompt.md](/home/romanzini/repos/locator360/.github/prompts/feature-planning.prompt.md)
- [.github/prompts/tdd-implementation.prompt.md](/home/romanzini/repos/locator360/.github/prompts/tdd-implementation.prompt.md)
- [.github/prompts/review.prompt.md](/home/romanzini/repos/locator360/.github/prompts/review.prompt.md)
- [.github/prompts/debug.prompt.md](/home/romanzini/repos/locator360/.github/prompts/debug.prompt.md)
- [.github/prompts/critical-flow.prompt.md](/home/romanzini/repos/locator360/.github/prompts/critical-flow.prompt.md)
- [.github/prompts/location-debug.prompt.md](/home/romanzini/repos/locator360/.github/prompts/location-debug.prompt.md)

Use esse conjunto quando quiser iniciar uma tarefa com menos ambiguidade, escolhendo o prompt pela intenção principal:

- regras do repositório
- planejamento
- implementação em TDD
- revisão
- debugging
- fluxo crítico

O `location-debug` permanece como exceção especializada porque o pipeline de localização tem um fluxo assíncrono e operacional próprio.