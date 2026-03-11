## Prompt Inicial para Implementação de Funcionalidade

"Vamos implementar a US-XXX. Domínio: <scope>. Regras de negócio: <...>.
Consulte Docs/especificacao-funcional.md e Docs/database-model.md antes de começar."

### Exemplo de Prompt para Implementação de Funcionalidade

"Vamos implementar a US-012 (Entrar em círculo com código/link).

Domínio: circle

Regras de negócio:

- Usuário autenticado envia um inviteCode via POST /api/v1/circles/join
- Validar que o convite existe, está PENDING e não expirou (expiresAt)
- Verificar limite de membros do plano (DEFAULT_MEMBER_LIMIT=5 por enquanto)
- Criar CircleMember com role=MEMBER e status=ACTIVE
- Atualizar o CircleInvite para status=ACCEPTED (setar acceptedByUserId)
- Publicar evento MEMBER_JOINED via NotificationCommandPublisher (Kafka topic: notification.commands)
  para todos os membros ativos do círculo (exceto o que acabou de entrar)

Contexto de código já existente:

- CircleInvite, InviteStatus, CircleMembershipService já implementados (US-011)
- CircleMember, CircleMemberRepository, CircleInviteRepository já existem
- Kafka topic notification.commands já configurado no docker-compose
- Port IN interface padrão: execute(UUID userId, InputDto): OutputDto

Consulte Docs/especificacao-funcional.md (seção Círculos), Docs/database-model.md
(tabelas circles, circle_members, circle_invites) e Docs/openapi.yaml
(endpoint POST /circles/join) antes de começar.

Siga o TDD: escreva os testes do JoinCircleService ANTES da implementação.
Sequência: PIN (UseCase + DTOs) → TEST (RED) → APP (GREEN) → INF (Kafka Publisher) → API → TEST Controller."

### Como preencher cada parte

| Placeholder | De onde tirar | Exemplo preenchido |
|---|---|---|
| US-XXX | backlog.md | US-012 |
| Domínio | Scope da US no backlog | circle |
| Regras de negócio | especificacao-funcional.md + coluna "Detalhes" do backlog | validações de convite, limite de membros |
| Contexto de código existente | O que já foi implementado em USs anteriores | US-011 entregou CircleInvite |
| Consulte... | Seções específicas dos docs relevantes para o domínio | tabelas circle_members, circle_invites |
| Sequência | Tarefas do backlog traduzidas em camadas | PIN → TEST → APP → INF → API |

A coluna "Detalhes" da tabela de tarefas no backlog é exatamente o conteúdo das regras de negócio — é só copiar e expandir no prompt.
