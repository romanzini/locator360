# Status de Implementação — User Stories

> Este arquivo rastreia o progresso de implementação de cada User Story do backlog.
> **Deve ser atualizado após cada US concluída** (merge na `main`).

## Legenda

| Status | Significado |
|--------|-------------|
| ✅ Done | Implementada, testada e mergeada na `main` |
| 🔧 In Progress | Em desenvolvimento |
| ⏳ Pending | Ainda não iniciada |

---

## Épico 1 – Gestão de Conta e Acesso

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-001 | Cadastro de conta | ✅ Done | (commits iniciais no main) | 2026-03-05 |
| US-002 | Login e manutenção de sessão | ✅ Done | #3 | 2026-03-09 |
| US-003 | Recuperação de senha | ✅ Done | #8, #9, #10, #11, #12, #13 | 2026-03-10 |
| US-004 | Atualizar perfil | ✅ Done | #15, #16, #17, #19, #20 | 2026-03-10 |

## Épico 2 – Círculos (Grupos Familiares)

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-010 | Criar círculo | ✅ Done | #25, #26, #27, #28, #29 | 2026-03-10 |
| US-011 | Convidar pessoas para o círculo | ✅ Done | #37 | 2026-03-11 |
| US-012 | Entrar em círculo com código/link | ✅ Done | #41, #42, #43, #44, #46, #47 | 2026-03-11 |
| US-013 | Gerenciar membros do círculo | ✅ Done | #52, #53, #54, #55, #56, #57 | 2026-03-11 |
| US-014 | Sair de um círculo | ✅ Done | #61, #62, #63 | 2026-03-13 |

## Épico 3 – Localização em Tempo Real

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-020 | Compartilhar localização com o círculo | ✅ Done | #77-#86 | 2026-03-16 |
| US-021 | Ver membros no mapa | ✅ Done | #91, #92, #93 | 2026-03-16 |
| US-022 | Pausar compartilhamento de localização | ✅ Done | #97 | 2026-03-16 |
| US-023 | Visualizar status de compartilhamento no mapa | ✅ Done | #103, #104, #105 | 2026-03-16 |

## Épico 4 – Lugares (Geofences) e Alertas

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-030 | Cadastrar um lugar | ✅ Done | (implemented in main) | 2026-03-26 |
| US-031 | Receber alertas de entrada/saída | ✅ Done | #127 | 2026-03-26 |
| US-032 | Configurar horários de relevância | ⏳ Pending | — | — |

## Épico 5 – Histórico de Localização

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-040 | Ver histórico de um dia | ⏳ Pending | — | — |
| US-041 | Restringir histórico por plano | ⏳ Pending | — | — |

## Épico 6 – Direção e Segurança no Trânsito

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-050 | Registrar viagens automaticamente | ⏳ Pending | — | — |
| US-051 | Avaliar comportamento de direção | ⏳ Pending | — | — |
| US-052 | Alertas de direção arriscada | ⏳ Pending | — | — |

## Épico 7 – SOS e Emergências

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-060 | Acionar SOS manual | ⏳ Pending | — | — |
| US-061 | Ver detalhes de SOS recebido | ⏳ Pending | — | — |

## Épico 8 – Detecção Automática de Incidentes

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-070 | Detectar colisão automaticamente | ⏳ Pending | — | — |

## Épico 9 – Comunicação e Check-ins

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-080 | Chat do círculo | ⏳ Pending | — | — |
| US-081 | Check-in manual | ⏳ Pending | — | — |

## Épico 10 – Notificações e Preferências

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-090 | Notificações de eventos | ⏳ Pending | — | — |
| US-091 | Personalizar notificações | ⏳ Pending | — | — |

## Épico 11 – Planos, Assinaturas e Billing

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-100 | Ver plano e benefícios | ⏳ Pending | — | — |
| US-101 | Assinar plano premium | ⏳ Pending | — | — |
| US-102 | Gerenciar planos disponíveis (CRUD Admin) | ⏳ Pending | — | — |

## Épico 12 – Privacidade e Segurança de Dados

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-110 | Controlar compartilhamento por círculo | ⏳ Pending | — | — |
| US-111 | Saber quem pode me ver | ⏳ Pending | — | — |

## Épico 13 – Administração e Suporte

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-120 | Consultar usuário no painel | ⏳ Pending | — | — |
| US-121 | Bloquear conta por abuso | ⏳ Pending | — | — |
| US-122 | Monitoramento e suporte operacional | ⏳ Pending | — | — |

## Histórias Técnicas

| US | Descrição | Status | PR(s) | Data |
|----|-----------|--------|-------|------|
| US-130 | Gerenciar identidades de autenticação | ⏳ Pending | — | — |
| US-131 | Tokens de verificação e recuperação | ⏳ Pending | — | — |
| US-132 | Gerenciar dispositivos e revogar sessão | ✅ Done | #69, #70, #71 | 2026-03-14 |
| US-133 | Configurar políticas de alerta por lugar | ⏳ Pending | — | — |
| US-134 | Persistir estado de compartilhamento por usuário e círculo | ⏳ Pending | — | — |
| US-135 | Marcações administrativas em contas | ⏳ Pending | — | — |
| US-136 | Auditar ações do backoffice | ⏳ Pending | — | — |

---

## Resumo

| Métrica | Valor |
|---------|-------|
| Total de User Stories | 43 |
| ✅ Done | 14 |
| 🔧 In Progress | 0 |
| ⏳ Pending | 29 |
| Progresso | 32.6% |
