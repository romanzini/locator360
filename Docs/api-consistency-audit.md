# Auditoria de Consistência — OpenAPI x Postman

**Data:** 2026-02-20  
**Escopo:** Comparação entre `Docs/openapi.yaml` e `Docs/postman-collection.json`.

## 1) Paridade de Paths

Método de comparação:

- extração automática de paths do OpenAPI;
- extração automática de `request.url.raw` da coleção Postman;
- normalização de placeholders (`{{param}}` → `{param}`) e remoção de query string para comparar apenas o path.

Resultado:

- Paths OpenAPI: **47**
- Paths Postman: **47**
- Somente no OpenAPI: **0**
- Somente no Postman: **0**

## 2) Paridade de Operações (Método + Path)

Método de comparação:

- análise de todas as operações `GET/POST/PUT/PATCH/DELETE` em cada path do OpenAPI;
- comparação com `request.method + request.url.raw` da coleção Postman (com mesma normalização de placeholders).

Resultado:

- Operações OpenAPI: **63**
- Operações Postman: **63**
- Faltando no Postman: **0**
- Extras no Postman: **0**

## 3) Correções Aplicadas para Fechar Gap de Operações

Requests adicionados na coleção Postman:

- `PATCH /users/me`
- `PATCH /circles/{circleId}`
- `DELETE /circles/{circleId}`
- `GET /circles/{circleId}/checkins`
- `GET /circles/{circleId}/messages/{messageId}/receipts`
- `PATCH /circles/{circleId}/places/{placeId}`
- `DELETE /circles/{circleId}/places/{placeId}`

## 4) Conclusão

A coleção Postman está **sincronizada com o OpenAPI** nos níveis de:

- cobertura de paths;
- cobertura de operações (método + path).

## 5) Paridade Semântica de Payloads (Campos Obrigatórios)

Método de comparação:

- para cada operação com `requestBody` referenciando schema em `#/components/schemas/*`, foram extraídos os campos `required` do schema;
- para cada request correspondente no Postman, foi feito parse do `body.raw` (JSON) e extração das chaves top-level;
- foi validado se todo campo obrigatório do schema está presente no payload de exemplo da coleção.

Resultado:

- Operações auditadas nessa regra: **14**
- Divergências encontradas: **0**

Limitação conhecida desta etapa:

- a validação foi feita em nível top-level de JSON (não valida obrigatoriedade aninhada em objetos internos nem regras `oneOf` complexas).

## 6) Conclusão Final

A coleção Postman está **sincronizada com o OpenAPI** em:

- paths;
- operações (método + path);
- presença de campos obrigatórios top-level nos payloads auditáveis por schema.

## 7) Auditoria Nível 2 (Tipos, Enum e Format)

Método de comparação:

- para operações com payload JSON parseável, foram comparados campos enviados no Postman com metadados top-level do schema no OpenAPI (`type`, `enum`, `format`, `nullable`);
- validações aplicadas: tipo primitivo, aderência a enum e formatos `email` / `date-time`.

Resultado:

- Operações auditadas nesta regra: **24**
- Inconsistências encontradas inicialmente: **1**
  - campo `mutedUntil` em `UpdateNotificationPreferencesRequest` aceitava `date-time`, mas não marcava `nullable`, enquanto o payload de exemplo usa `null`.
- Ação corretiva aplicada: `nullable: true` em `UpdateNotificationPreferencesRequest.mutedUntil` em `openapi.yaml`.
- Resultado após correção: **0 inconsistências**.

Limitações conhecidas desta etapa:

- validação restrita ao nível top-level dos payloads;
- não cobre validação profunda de estruturas aninhadas com regras combinadas (`oneOf`/`allOf`) em subobjetos.

## 8) Top 4 Lacunas Técnicas Remanescentes (Prioridade)

1. **P1 — Ausência de governança explícita de `admin_users` (CRUD/admin lifecycle)**

- Situação: existe operação administrativa sobre usuários finais, flags e auditoria, mas não há recurso dedicado para gestão de contas administrativas.
- Risco: controle incompleto de ciclo de vida de operadores internos e menor rastreabilidade de permissões.
- Recomendação: adicionar endpoints mínimos de `admin_users` (listar, criar, bloquear/desbloquear, trocar papel), com trilha em `audit_logs`.

1. **P1 — Validação semântica ainda parcial para payloads aninhados e `oneOf`**

- Situação: a auditoria automatizada validou top-level, mas não cobre regras profundas de subobjetos/composições.
- Risco: inconsistências silenciosas em payloads complexos (ex.: objetos anexados de localização e cenários alternativos de autenticação).
- Recomendação: criar suíte de validação JSON Schema/OpenAPI por operação crítica no CI.

1. **P2 — `place_alert_targets` sem recurso dedicado (apenas embutido em policy)**

- Situação: coberto de forma indireta via `targetUserIds` em `place_alert_policies`.
- Risco: menor granularidade para auditoria e operações incrementais de destinatários.
- Recomendação: avaliar endpoint dedicado de targets (listar/adicionar/remover) se houver necessidade operacional.

1. **P3 — Falta de contrato de paginação/filtros padronizado para listas volumosas**

- Situação: endpoints de listagem existem, mas sem padrão transversal explícito de paginação/ordenação em todos os recursos.
- Risco: inconsistência de consumo e degradação de performance em grandes volumes.
- Recomendação: padronizar convenção (`page`, `pageSize`, `sort`, `cursor`) e aplicar progressivamente nos recursos de histórico/eventos.

Nota de fechamento: a padronização da nomenclatura de direção para `drive` foi concluída em todos os documentos do projeto em 2026-02-20.

## 9) Decisão de Escopo — Driving v1

- Decisão: o módulo de direção da versão atual considera apenas viagens com `mode = CAR`.
- Vigência: 2026-02-20.
- Impacto documental aplicado: remoção de referências operacionais a `WALK` e `BIKE` no domínio de direção, incluindo modelo de dados, estratégia de detecção e pseudo-código.
- Observação de produto: expansão para outros modos de deslocamento deve ser tratada como escopo futuro explícito, com revisão de requisitos e contrato de API.
