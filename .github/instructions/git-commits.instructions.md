---
applyTo: '**'
---

# Git Commits — Regras Obrigatórias

> Todo código commitado **DEVE** seguir estas diretrizes de commits atômicos e small releases. Commits grandes e genéricos **NÃO** serão aceitos.

## 📐 Filosofia: Small Releases

O projeto adota a prática de **small releases** — cada commit deve representar uma **unidade mínima de trabalho funcional**, completa e independente. Isso garante:

- **Rastreabilidade**: Cada mudança é fácil de entender e revisar
- **Reversibilidade**: Qualquer commit pode ser revertido sem impacto colateral
- **Integração contínua**: Commits pequenos reduzem conflitos e facilitam merge
- **Histórico limpo**: O `git log` conta a história do projeto de forma clara

## 📝 Conventional Commits — Formato Obrigatório

### Estrutura da Mensagem
```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Tipos Permitidos

| Tipo | Uso | Exemplo |
|------|-----|---------|
| `feat` | Nova funcionalidade | `feat(auth): add email registration endpoint` |
| `fix` | Correção de bug | `fix(circle): fix member removal validation` |
| `refactor` | Refatoração sem mudança de comportamento | `refactor(location): extract geofence calculation` |
| `test` | Adição/modificação de testes | `test(auth): add RegisterUserService unit tests` |
| `docs` | Documentação | `docs(api): update OpenAPI spec for circles` |
| `chore` | Tarefas de build, CI, dependências | `chore: add micrometer-prometheus dependency` |
| `style` | Formatação, espaços, imports | `style(auth): organize imports` |
| `perf` | Melhoria de performance | `perf(location): optimize batch insert query` |
| `ci` | Configuração de CI/CD | `ci: add GitHub Actions workflow` |
| `build` | Mudanças no build system | `build: upgrade Spring Boot to 3.5.1` |
| `migration` | Migrations de banco de dados | `migration(users): create users table` |

### Scopes por Domínio

Use o domínio de negócio como scope:

| Scope | Domínio |
|-------|---------|
| `auth` | Auth & Account (users, auth_identities, verification_tokens) |
| `circle` | Circles & Members |
| `location` | Location & History |
| `place` | Places & Geofence |
| `drive` | Driving & Safety |
| `sos` | SOS & Incidents |
| `chat` | Chat & Checkin |
| `notification` | Notifications |
| `plan` | Billing & Plans |
| `admin` | Admin & Audit |
| `infra` | Infraestrutura cross-cutting (config, docker, CI) |

### Regras de Mensagem
- **Idioma**: Inglês para mensagem do commit
- **Descrição**: Imperativo, presente, lowercase, sem ponto final
- **Máximo**: 72 caracteres na primeira linha
- **Body**: Opcional — use para explicar o "porquê" quando não óbvio
- **Footer**: Use para referências (`Refs: #123`) ou breaking changes (`BREAKING CHANGE:`)

```
# ✅ CORRETO
feat(auth): add email registration endpoint
fix(circle): prevent duplicate member invitations
test(auth): add unit tests for RegisterUserService
migration(users): create users and auth_identities tables
chore: add spring-boot-starter-actuator dependency

# ❌ ERRADO
Added new feature                    # Sem tipo, sem scope, vago
feat: lots of changes                # Commit grande demais
FEAT(AUTH): Add Registration.        # Não usar maiúsculas, sem ponto final
feat(auth): added email registration # Use imperativo, não passado
```

## 🎯 Granularidade dos Commits — Small Releases

### Regra de Ouro
> **Um commit = uma responsabilidade**. Se você precisa usar "e" para descrever o commit, provavelmente são dois commits.

### Commits por Camada Arquitetural

Seguindo a Arquitetura Vexa, cada camada gera commits independentes:

| Ordem | Camada | Commit Pattern | Exemplo |
|-------|--------|---------------|---------|
| 1 | Database | `migration(<domain>): ...` | `migration(auth): create users table` |
| 2 | Domain | `feat(<domain>): ...` | `feat(auth): add User domain entity` |
| 3 | Tests (Domain) | `test(<domain>): ...` | `test(auth): add User entity unit tests` |
| 4 | Port OUT | `feat(<domain>): ...` | `feat(auth): add UserRepository port` |
| 5 | Port IN + DTOs | `feat(<domain>): ...` | `feat(auth): add RegisterUserUseCase port and DTOs` |
| 6 | Tests (Application) | `test(<domain>): ...` | `test(auth): add RegisterUserService unit tests` |
| 7 | Application | `feat(<domain>): ...` | `feat(auth): implement RegisterUserService` |
| 8 | Infrastructure | `feat(<domain>): ...` | `feat(auth): implement UserJpaRepository adapter` |
| 9 | Tests (API) | `test(<domain>): ...` | `test(auth): add AuthController unit tests` |
| 10 | API | `feat(<domain>): ...` | `feat(auth): add POST /auth/register endpoint` |

### Exemplos de Small Releases para uma User Story

Para a US-001 (Cadastro de conta), os commits seriam:

```bash
# Migrations
migration(auth): create users table
migration(auth): create auth_identities table
migration(auth): create verification_tokens table

# Domain
feat(auth): add UserStatus enum
feat(auth): add AuthProvider and TokenType enums
feat(auth): add User domain entity with factory methods
feat(auth): add AuthIdentity domain entity
feat(auth): add VerificationToken domain entity

# Tests Domain
test(auth): add User entity unit tests
test(auth): add AuthIdentity entity unit tests
test(auth): add VerificationToken entity unit tests

# Ports
feat(auth): add UserRepository port
feat(auth): add AuthIdentityRepository port
feat(auth): add VerificationTokenRepository port
feat(auth): add RegisterUserUseCase port and DTOs

# Application
test(auth): add RegisterUserService unit tests
feat(auth): implement RegisterUserService

# Infrastructure
feat(auth): implement UserJpaEntity
feat(auth): implement AuthIdentityJpaEntity
feat(auth): implement VerificationTokenJpaEntity
feat(auth): implement UserJpaRepository adapter
feat(auth): implement AuthIdentityJpaRepository adapter
feat(auth): implement VerificationTokenJpaRepository adapter

# API
test(auth): add AuthController unit tests
feat(auth): add AuthControllerApi with OpenAPI annotations
feat(auth): implement AuthController
```

### ⚠️ O que NÃO fazer

```bash
# ❌ Commit gigante com várias camadas
feat(auth): implement user registration feature

# ❌ Commit vago sem contexto
feat: add new files

# ❌ Múltiplas responsabilidades
feat(auth): add User entity, repository, and controller

# ❌ Testes junto com implementação (a menos que TDD RED→GREEN no mesmo commit)
feat(auth): add RegisterUserService with tests
```

### ✅ Agrupamentos Aceitáveis

Commits podem agrupar itens quando são **da mesma camada e altamente coesos**:

```bash
# ✅ Enums relacionados no mesmo commit
feat(auth): add UserStatus, AuthProvider, and TokenType enums

# ✅ DTOs de entrada/saída do mesmo use case
feat(auth): add RegisterUserUseCase port and DTOs

# ✅ JPA Entity + Spring Data interface (sempre andam juntos)
feat(auth): implement UserJpaEntity and Spring Data repository
```

## 🔄 Workflow de Commit

### Antes de Commitar
1. **Verificar escopo**: O commit altera apenas UMA responsabilidade?
2. **Testes passando**: Todos os testes existentes continuam verdes?
3. **Sem código morto**: Remover imports não utilizados, código comentado
4. **Mensagem clara**: A mensagem descreve exatamente o que foi feito?

### Fluxo TDD e Commits
Seguindo o TDD obrigatório do projeto, o fluxo recomendado é:

```
1. Escrever testes (RED)      → commit: test(<scope>): add <component> unit tests
2. Implementar (GREEN)        → commit: feat(<scope>): implement <component>
3. Refatorar (REFACTOR)       → commit: refactor(<scope>): <description>   (se houver mudança significativa)
```

Alternativamente, quando o teste e a implementação são muito pequenos:
```
1. RED + GREEN juntos         → commit: feat(<scope>): implement <component>  (com testes incluídos)
2. REFACTOR                   → commit: refactor(<scope>): <description>
```

## 📋 Checklist de Commit

Antes de cada `git commit`, verifique:

- [ ] Mensagem segue o formato `<type>(<scope>): <description>`
- [ ] Descrição em inglês, imperativo, lowercase, sem ponto final
- [ ] Commit tem uma única responsabilidade
- [ ] Todos os testes passam
- [ ] Sem código comentado ou imports não utilizados
- [ ] Nenhum dado sensível (senhas, tokens, secrets) no código
- [ ] Arquivos desnecessários não estão sendo commitados (`.gitignore` atualizado)

## 🔀 Workflow de PR — 1 Commit = 1 Branch = 1 PR

> Cada commit atômico **DEVE** gerar um PR individual. PRs gigantes com múltiplas responsabilidades **NÃO** serão aceitos.

### Fluxo Obrigatório

Para cada unidade de trabalho (commit), execute:

```bash
# 1. Garantir main atualizada
git checkout main
git pull origin main

# 2. Criar branch com nome padronizado
git checkout -b <type>/<scope>-<descricao-curta>

# 3. Implementar, stage e commit
git add <arquivos>
git commit -m "<type>(<scope>): <description>"

# 4. Push e criar PR com auto-merge
git push -u origin HEAD
gh pr create --base main --head <branch> --title "<type>(<scope>): <description>" --body "<descrição>"
gh pr merge <branch> --auto --rebase --delete-branch

# 5. Voltar para main e aguardar merge do CI
git checkout main
# (aguardar CI build-and-test finalizar — auto-merge acontece automaticamente)
git pull origin main
```

### Convenção de Nomes de Branch

O nome da branch segue o mesmo padrão do commit:

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Feature | `feat/<scope>-<desc>` | `feat/auth-login-service` |
| Fix | `fix/<scope>-<desc>` | `fix/circle-duplicate-invite` |
| Test | `test/<scope>-<desc>` | `test/auth-login-tests` |
| Migration | `migration/<scope>-<desc>` | `migration/auth-devices-table` |
| Refactor | `refactor/<scope>-<desc>` | `refactor/location-geofence` |
| Chore | `chore/<desc>` | `chore/add-prometheus-dep` |
| Style | `style/<scope>-<desc>` | `style/auth-formatting` |
| Docs | `docs/<scope>-<desc>` | `docs/api-openapi-update` |

### Regras do PR
- **Título**: Idêntico à mensagem do commit (`<type>(<scope>): <description>`)
- **Body**: Breve descrição do que foi feito e por quê (1-3 linhas)
- **Base**: Sempre `main`
- **Merge strategy**: `--rebase` (linear history)
- **Auto-merge**: Sempre usar `--auto` para merge automático após CI
- **Delete branch**: Sempre usar `--delete-branch` para limpeza automática

### Verificação Durante Espera do CI

```bash
# Ver status dos PRs abertos
gh pr status

# Ver status do CI em um PR específico
gh pr checks <número-do-pr>
```

### ⚠️ Importante
- **Sempre** `git pull origin main` antes de criar nova branch
- **Nunca** criar branch a partir de outra branch de feature
- **Nunca** acumular múltiplos commits em uma branch (exceto se altamente coesos na mesma camada)
- Se o CI falhar, corrija na mesma branch, faça amend e force-push
