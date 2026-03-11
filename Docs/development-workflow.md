# Locator 360 — Workflow de Desenvolvimento

> Guia de referência para implementar e evoluir o projeto com IA, TDD e Small Releases.

---

## Filosofia: Extreme Programming com IA

Este projeto adota **XP (Extreme Programming)** como base, com o agente de IA atuando como **segundo dev** no modelo de Pair Programming. As práticas centrais são:

| Prática | Como aplicamos |
|---------|---------------|
| **Pair Programming** | Você é o Dev; a IA é o Piloto (sugere, implementa, revisa) |
| **Small Releases** | 1 commit = 1 responsabilidade = 1 PR — nunca commits grandes |
| **TDD (obrigatório)** | Testes **antes** do código — sem testes, cada mudança é uma aposta |
| **Engenharia de Qualidade** | CI com linters, cobertura, validação de vulnerabilidades |
| **Primeiro Prompt** | Antes de começar qualquer US, defina o domínio e os testes no primeiro prompt à IA |

> **TDD é mais importante que a IA.** O agente modifica código sem confiança porque não tem nada de segurança. Sem testes, cada mudança é uma aposta.

---

## A IA como Agente no Terminal

O agente tem acesso ao sistema de arquivos e terminal, com as seguintes capacidades e restrições:

```
Agente no Terminal
├── Acesso ao sistema de arquivos do projeto
├── Execução de testes (via Docker Maven)
├── Memória
│   └── Contexto compartilhado → Refatoração multi-serviço → Execução dos testes criados
└── Sandbox & Segurança
    ├── Docker Container (isolamento)
    └── GitHub (Permissions Model — PR obrigatório, CI obrigatório)
```

A IA **não faz push direto para main**. Todo código passa por PR + CI verde.

---

## Os 7 Passos do Desenvolvimento com IA

### Step 1 — AI Init

Antes de qualquer linha de código, configure o contexto do agente:

- [ ] Definir **domínio e governança** — qual bounded context, quais regras de negócio
- [ ] Verificar **config e dependências** — `pom.xml`, `application.yml`, docker-compose
- [ ] Carregar **memória** do projeto — arquivos em `/memories/repo/`
- [ ] Revisar **`instructions.md`** — arquitetura Vexa, regras de commits, observabilidade

```
Prompt inicial modelo:
"Vamos implementar a US-XXX. Domínio: <scope>. Regras de negócio: <...>.
Consulte Docs/especificacao-funcional.md e Docs/database-model.md antes de começar."
```

### Step 2 — A Fundação (Arquitetura)

Criar a estrutura de pastas e arquivos base do domínio:

- [ ] Checar se a migration é necessária (`Docs/database-model.md`)
- [ ] Criar estrutura de pacotes se novo domínio
- [ ] Definir enums e value objects

Nunca pular para código antes de ter a fundação clara.

### Step 3 — TDD (Testes Primeiro — SEMPRE)

> **A IA escreve os TESTES antes do código.**  
> Se a IA sugerir uma função sem testes, **null testa** — rejeite e peça os testes primeiro.

Fluxo obrigatório:

```
RED  → Escrever os testes (falham porque o código não existe)
GREEN → Implementar o mínimo para passar
REFACTOR → Melhorar sem quebrar testes
```

Cada fase gera um commit:

- `test(<scope>): add <component> unit tests` → RED
- `feat(<scope>): implement <component>` → GREEN
- `refactor(<scope>): <description>` → REFACTOR (se houver)

### Step 4 — Código

Implementar seguindo a sequência de camadas (de dentro para fora):

```
Migration → Domain → Port OUT → Port IN + DTOs → Service → Infrastructure → Controller
```

> **Não ao magic!** Sem mapeamentos automáticos que você não controla, sem anotações "mágicas" sem intenção explícita. Cada decisão deve ser compreendida e testada.

### Step 5 — Otimização

Após os testes passarem, avaliar:

- [ ] **Processamento pesado e jobs** — mover para Kafka consumers se for assíncrono
- [ ] **Refactoring** — aplicar apenas se os testes estiverem verdes e a mudança for clara
- [ ] Nunca otimizar prematuramente — só otimize o que é lento E comprovadamente problemático

### Step 6 — Interfaces

Expor a funcionalidade para o mundo externo:

- [ ] **Web / Mobile / Bot** — definir contrato REST (OpenAPI)
- [ ] Configurar **servidor de produção** se necessário (Dockerfile, env vars)
- [ ] Deploy — validar que a aplicação sobe corretamente

### Step 7 — Deploy & Qualidade

Antes de considerar a US finalizada:

- [ ] **Scripts de CI/CD** — GitHub Actions rodando
- [ ] **Validação de código** — linters, code quality, imports não utilizados
- [ ] **Testes** — todos os testes da US passando no CI
- [ ] **Vulnerabilidades** — sem secrets no código, sem dependências vulneráveis conhecidas

---

## Implementação de uma US — Passo a Passo

> Guia de referência para implementar uma nova User Story do zero até o merge.

---

### US-0. Definir a US no Backlog

Abra `Docs/backlog.md` e adicione a US no formato abaixo:

```markdown
## US-XXX – Nome da funcionalidade

**Como** <persona>
**Quero** <ação>
**Para** <benefício>

### Tarefas
- [ ] **T1** `migration(<scope>)` – Criar migration Flyway (se necessário)
- [ ] **T2** `feat(<scope>)` – Entidades de domínio / Enums
- [ ] **T3** `test(<scope>)` – Testes unitários das entidades de domínio
- [ ] **T4** `feat(<scope>)` – Ports OUT (interfaces de repositório)
- [ ] **T5** `feat(<scope>)` – Ports IN (use case interface) + DTOs input/output
- [ ] **T6** `test(<scope>)` – Testes unitários do Application Service
- [ ] **T7** `feat(<scope>)` – Application Service (implementação)
- [ ] **T8** `feat(<scope>)` – Infrastructure (JPA entity + repository adapter)
- [ ] **T9** `test(<scope>)` – Testes do Controller
- [ ] **T10** `feat(<scope>)` – Endpoint no Controller
```

---

### US-1. Sequência de Implementação (por camada)

Seguir sempre de **dentro para fora** — domínio primeiro, API por último:

```
Migration → Domain → Test Domain → Port OUT → Port IN + DTOs → Test Service → Service → Infrastructure → Test Controller → Controller
```

### Camada 1 — Migration (se houver nova tabela)

Criar em `src/main/resources/db/migration/`:

```sql
-- VN__create_<table>.sql
CREATE TABLE <table> (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ...
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### Camada 2 — Domain

Criar entidade em `src/main/java/com/locator360/core/domain/<scope>/`:

- Use **private constructor** + factory methods `create()` e `restore()`
- `create()` para novas instâncias com geração de ID / timestamps
- `restore()` para reconstituição a partir do banco
- Sem Lombok nas entidades de domínio — getters manuais
- Adicione enums no mesmo pacote

### Camada 3 — Port OUT

Criar interface em `src/main/java/com/locator360/core/port/out/`:

```java
public interface <Entity>Repository {
    <Entity> save(<Entity> entity);
    Optional<<Entity>> findById(UUID id);
    // métodos específicos do domínio
}
```

### Camada 4 — Port IN + DTOs

Criar interface em `src/main/java/com/locator360/core/port/in/<scope>/`:

```java
public interface <Action>UseCase {
    <OutputDto> execute(UUID userId, <InputDto> input);
}
```

Criar DTOs em `src/main/java/com/locator360/core/port/in/dto/`:

- **Input**: `@Data @NoArgsConstructor` com validações `@NotBlank`, `@Email`, etc.
- **Output**: `@Value @Builder` (imutável)

> ⚠️ **Obrigatório:** registrar o mapeamento do output DTO em `ModelMapperConfig.java`
> usando `.setConverter()` com o builder — nunca contar com o mapeamento automático.

```java
modelMapper.createTypeMap(<Entity>.class, <Output>Dto.class)
    .setConverter(ctx -> {
        <Entity> src = ctx.getSource();
        return <Output>Dto.builder()
            .id(src.getId())
            // ... todos os campos
            .build();
    });
```

### Camada 5 — Application Service (TDD)

**RED primeiro:** escrever `<Action>ServiceTest.java` em `src/test/.../service/<scope>/`

Casos mínimos a cobrir:

- Fluxo feliz (retorno correto)
- Variações de input válido
- Erro: entidade não encontrada
- Erro: validação de negócio
- Verificação de dependências (verify mocks)
- Verificação de métricas (meterRegistry)

**GREEN:** implementar `<Action>Service.java` em `src/main/java/.../application/service/<scope>/`:

```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class <Action>Service implements <Action>UseCase {

    @Override
    public <OutputDto> execute(...) {
        log.debug("...");
        // lógica de negócio
        meterRegistry.counter("<domain>.<action>").increment();
        log.info("...");
        return modelMapper.map(result, <OutputDto>.class);
    }
}
```

### Camada 6 — Infrastructure

Criar em `src/main/java/com/locator360/infrastructure/persistence/postgresql/`:

1. **JPA Entity** (`entity/`) — `@Entity @Table @Slf4j`, campos com `@Column`
2. **Spring Data interface** (`repository/`) — `extends JpaRepository<JpaEntity, UUID>`
3. **Repository Adapter** (`repository/`) — implementa Port OUT, usa `restore()` para converter JPA → Domain

### Camada 7 — API (Controller)

Adicionar endpoint em `CircleController` (ou controller do domínio):

1. Método na interface `<Domain>ControllerApi` com anotações OpenAPI
2. Implementação no `<Domain>Controller` extraindo `userId` do `SecurityContext`

```java
@Override
public ResponseEntity<<OutputDto>> <action>(...) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received <action> request from user: {}", userId);
    <OutputDto> output = <action>UseCase.execute(userId, ...);
    log.info("<Action> completed: {}", output.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(output);
}
```

---

### US-2. Workflow Git — 1 Tarefa = 1 Branch = 1 PR

Para **cada tarefa** da US:

```powershell
# 1. Partir sempre de main atualizada
git checkout main
git pull origin main

# 2. Criar branch
git checkout -b <type>/<scope>-<descricao-curta>

# 3. Implementar, stage e commit
git add <arquivos>
git commit -m "<type>(<scope>): <description>"

# 4. Push + criar PR
git push -u origin HEAD
gh pr create --base main --head (git branch --show-current) `
  --title "<type>(<scope>): <description>" `
  --body "<descrição breve>"

# 5. Aguardar CI e fazer merge
gh pr checks <número> --watch
gh pr merge (git branch --show-current) --rebase --delete-branch --admin

# 6. Voltar para main antes da próxima tarefa
git checkout main
git pull origin main
```

### Convenção de nomes de branch

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Feature | `feat/<scope>-<desc>` | `feat/circle-get-user-circles` |
| Fix | `fix/<scope>-<desc>` | `fix/circle-invite-modelmapper` |
| Test | `test/<scope>-<desc>` | `test/circle-service-tests` |
| Migration | `migration/<scope>-<desc>` | `migration/circle-invites-table` |

---

### US-3. Checklist por Tarefa

Antes de cada commit:

- [ ] Testes passando (`mvn test`)
- [ ] `@Slf4j` presente em todas as classes de produção
- [ ] `log.debug` na entrada dos métodos públicos
- [ ] `log.info` para eventos de negócio
- [ ] `log.error` com exceção em blocos catch
- [ ] Sem dados sensíveis nos logs
- [ ] Métricas de negócio para operações críticas (`meterRegistry.counter(...)`)
- [ ] Output DTO com converter registrado em `ModelMapperConfig`
- [ ] Sem código comentado ou imports não utilizados
- [ ] Mensagem de commit no formato `<type>(<scope>): <description>`

---

### US-4. Adicionar comando de teste manual em `commands.md`

Ao fim da US, adicionar na seção "7. Testar os endpoints":

```powershell
### US-XXX: <descrição>

$result = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/<path>" `
  -Method <METHOD> -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -Body '<json>'

$result | ConvertTo-Json -Depth 5
```

E adicionar o comando de testes automatizados:

```powershell
### US-XXX: rodar testes automatizados do pacote

docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=<Test1>,<Test2>,<Test3>" test "-Dsurefire.useFile=false"
```

---

### US-5. Documentos de Referência

Antes de implementar, consultar:

| Documento | Quando consultar |
|-----------|-----------------|
| `Docs/especificacao-funcional.md` | Regras de negócio, validações, personas |
| `Docs/database-model.md` | Tabelas, colunas, tipos, constraints |
| `Docs/openapi.yaml` | Contratos de entrada/saída dos endpoints |
| `Docs/backlog.md` | Tarefas e escopo da US |
| `.github/instructions/vexa-architecture.instructions.md` | Regras de arquitetura |
| `.github/instructions/observability.instructions.md` | Logging, métricas, health |
| `.github/instructions/git-commits.instructions.md` | Formato de commits e PRs |
