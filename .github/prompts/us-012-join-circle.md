# Prompt — US-012: Entrar em círculo com código/link

"Vamos implementar a US-012 (Entrar em círculo com código/link).

Domínio: circle

Regras de negócio:

- Usuário autenticado envia um inviteCode via POST /api/v1/circles/join
- Validar que o convite existe no banco (pelo inviteCode) — lançar exceção se não encontrado
- Validar que o convite está com status=PENDING — lançar exceção se já ACCEPTED ou EXPIRED
- Validar que o convite não expirou (expiresAt < Instant.now()) — lançar exceção se vencido
- Verificar limite de membros do plano: DEFAULT_MEMBER_LIMIT = 5 (constante por enquanto) — lançar exceção se limite atingido
- Criar CircleMember usando fábrica CircleMember.createMember(circleId, userId) — role=MEMBER, status=ACTIVE
- Salvar o novo CircleMember via CircleMemberRepository
- Atualizar o CircleInvite: setar status=ACCEPTED e acceptedByUserId=userId, salvar via CircleInviteRepository
- Publicar evento MEMBER_JOINED via NotificationCommandPublisher no tópico notification.commands
  para TODOS os membros ATIVOS do círculo, exceto o usuário que acabou de entrar
  — payload: Map contendo 'newMemberUserId' (UUID), 'circleId' (UUID)

Contexto de código já existente:

- US-011 implementou: CircleInvite, InviteStatus (PENDING, ACCEPTED, EXPIRED), CircleInviteRepository (findByInviteCode, save), CircleMember, CircleMemberRepository (save, findByCircleIdAndUserId, findByCircleId, countByCircleId), CircleRole, MemberStatus
- CircleMember.createAdmin() e CircleMember.createMember() factory methods já existem
- CircleMember.restore() já existe para reconstituição
- CreateInviteService (US-011) é bom exemplo de Application Service com ModelMapper
- ModelMapperConfig.java requer converter explícito para todo DTO com @Value @Builder (padrão obrigatório — ver converter de CircleOutputDto como referência)
- Kafka topic notification.commands já está declarado no docker-compose.yml

Sequência de implementação (1 commit = 1 branch = 1 PR):

1. feat(circle): add JoinCircleUseCase port, JoinCircleInputDto, CircleMemberOutputDto
   - Interface JoinCircleUseCase: execute(UUID userId, JoinCircleInputDto): CircleMemberOutputDto
   - JoinCircleInputDto: campos inviteCode (String, @NotBlank)
   - CircleMemberOutputDto: campos id, circleId, userId, role, status, joinedAt (String ou Instant)

2. feat(circle): add NotificationCommandPublisher port, NotificationCommand, NotificationType
   - NotificationType enum: MEMBER_JOINED (outros virão no futuro: MEMBER_REMOVED, MEMBER_LEFT, etc.)
   - NotificationCommand: tipo imutável, campos: type (NotificationType), recipientUserId (UUID), circleId (UUID nullable), payload (Map<String, Object>)
   - Porta OUT NotificationCommandPublisher: publish(NotificationCommand): void

3. feat(circle): add findActiveByCircleId to CircleMemberRepository
   - Adicionar método: List<CircleMember> findActiveByCircleId(UUID circleId)
   - Necessário para buscar destinatários do MEMBER_JOINED

4. test(circle): add JoinCircleService unit tests
   - Testar código inexistente → exceção
   - Testar convite já ACCEPTED → exceção
   - Testar convite expirado pelo status EXPIRED → exceção
   - Testar convite com expiresAt passado → exceção
   - Testar limite de membros atingido → exceção
   - Testar fluxo feliz: CircleMember criado, invite atualizado, MEMBER_JOINED publicado para cada membro ativo do círculo
   - Verificar com Mockito que NotificationCommandPublisher.publish() é chamado N vezes (um por membro ativo, exceto o novo)

5. feat(circle): implement JoinCircleService
   - Implementar GREEN para todos os testes do passo 4
   - Annotações: @Service, @RequiredArgsConstructor, @Transactional, @Slf4j
   - Retornar CircleMemberOutputDto via modelMapper.map(savedMember, CircleMemberOutputDto.class)

6. feat(circle): add CircleMemberOutputDto converter to ModelMapperConfig
   - Adicionar converter explícito modelMapper.createTypeMap(CircleMember.class, CircleMemberOutputDto.class).setConverter(...)

7. feat(circle): implement KafkaNotificationCommandPublisher
   - Na camada infrastructure/event/kafka/publisher/
   - Implementa NotificationCommandPublisher
   - Publica no tópico notification.commands com chave de particionamento = circleId.toString()
   - Usar KafkaTemplate<String, Object>
   - Anotações: @Component, @RequiredArgsConstructor, @Slf4j

8. test(circle): add CircleController unit tests for POST /circles/join

9. feat(circle): add POST /api/v1/circles/join endpoint to CircleController
   - Receber @RequestBody @Valid JoinCircleInputDto
   - Extrair userId do SecurityContext (como já feito nos outros endpoints do CircleController)
   - Retornar 200 OK com CircleMemberOutputDto

Consulte antes de implementar:

- Docs/especificacao-funcional.md: seção 5.2 — Módulo Círculos (RF-014 a RF-017, RF-016A)
- Docs/database-model.md: tabelas circle_members (linha ~134) e circle_invites (linha ~157)
- Docs/openapi.yaml: endpoint POST /circles/join (verificar se há spec definida)
- CreateInviteService.java: padrão de Application Service do mesmo domínio (US-011)
- ModelMapperConfig.java: padrão obrigatório de converter para CircleOutputDto (referência)

Siga TDD estritamente: escreva os testes do passo 4 ANTES de implementar o passo 5.
Cada passo = 1 branch = 1 PR. Faça `git pull origin main` antes de cada nova branch."
