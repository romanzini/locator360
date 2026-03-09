---
applyTo: '**'
---

# Arquitetura de referência

## Fundamentos

A **Arquitetura Vexa** é uma evolução baseada na **Arquitetura Hexagonal (Ports & Adapters)**, otimizada para o **Spring Framework**. Ela reduz o acoplamento e aumenta a coesão, resultando em sistemas resilientes e fáceis de evoluir.

### Princípios Fundamentais
- **Baseada na Arquitetura Hexagonal**: Núcleo isolado de tecnologias externas
- **Spring-Centric**: Aproveitamento completo do ecossistema Spring (IoC, AOP, persistência)
- **Interação via "Portas"**: Interfaces bem definidas para entrada e saída

## Estrutura de Camadas

### **Camada API** (Adaptadores de Entrada)
Responsável por receber requisições externas e encaminhá-las para a camada de aplicação.

**Adaptadores Suportados:**
- **REST**: APIs RESTful (HTTP)
- **SOAP**: Web Services SOAP
- **Kafka**: Mensageria/Eventos de entrada

### **Camada Core** (Núcleo da Aplicação)
Contém toda a lógica de negócio e definições de contratos.

**Componentes Principais:**
- **Port**: Interfaces para os adaptadores de entrada e saída
- **Application**: Implementa os casos de uso e serviços de aplicação a partir dos ports (adaptadores de entrada)
- **Domain**: Entidades, serviços de domínio e value objects

### **Camada Infrastructure** (Adaptadores de Saída)
Implementa comunicação com sistemas externos.

**Adaptadores Suportados:**
- **Persistence**: Bancos de dados (JPA/Spring Data)
- **REST**: Clientes para APIs externas
- **SOAP**: Clientes para Web Services
- **Event**: Publishers de eventos (Kafka, RabbitMQ)

### **Shared** (Opcional)
Classes, funções e configurações compartilhadas entre módulos.

## 📋 Regras de Estrutura

### Criação de Novas Pastas
✅ **Permitido:** Criar subpastas dentro da hierarquia existente
```text
api/rest/security/      # ✅ Dentro de rest
core/domain/model/      # ✅ Dentro de domain
```

❌ **Não Permitido:** Criar pastas fora da hierarquia
```text
api/security/           # ❌ Não segue hierarquia
core/custom/            # ❌ Fora do padrão
```

### Pasta Config
Cada camada pode ter sua pasta `config`:
```text
api/rest/config/        # Configurações REST
core/application/config/ # Configurações de aplicação
infrastructure/persistence/config/ # Configurações de persistência
```

## 🔄 Regras Arquiteturais

### Direção das Dependências
```
API → Core ← Infrastructure
```
- **API** depende de **Core**
- **Infrastructure** depende de **Core** 
- **Core** não depende de nada externo

### Interação via Ports
- **Core** implementa as interfaces dos Ports IN e apenas utiliza as interfaces dos Ports OUT
- Implementações dos Ports OUT ficam na camada **Infrastructure**
- Inversão de dependência total

### Isolamento de Modelos
- **Domain Models**: Apenas no Core
- **DTOs**: Separados por entrada/saída
- **JPA Entities**: Apenas na Infrastructure

## 🎯 Padrões Obrigatórios

### Observabilidade (Obrigatória)
- **`@Slf4j` obrigatório**: Em todas as classes de produção (Controllers, Services, Repositories, Consumers, Publishers)
- **Logging estruturado**: `log.debug` na entrada, `log.info` para eventos de negócio, `log.error` com exceção
- **Métricas de negócio**: Counters e Timers via `MeterRegistry` para operações críticas
- **Health checks**: Atuator habilitado com endpoints `/actuator/health`, `/actuator/prometheus`
- **Segurança nos logs**: Nunca logar senhas, tokens, dados sensíveis
- **Instruções especializadas:** [`Observability Instructions`](observability.instructions.md)

### Git Commits (Small Releases Obrigatório)
- **Conventional Commits**: Formato `<type>(<scope>): <description>` obrigatório
- **Um commit = uma responsabilidade**: Cada commit altera apenas UMA camada ou concern
- **Small releases**: Commits atômicos, reversíveis e independentes
- **TDD + Commit**: RED → commit de teste, GREEN → commit de implementação
- **Inglês**: Mensagens de commit sempre em inglês
- **Instruções especializadas:** [`Git Commits Instructions`](git-commits.instructions.md)

### Fluxo de Desenvolvimento (TDD Obrigatório)
- **Testes primeiro sempre**: antes de qualquer código de produção de uma funcionalidade, implemente todos os testes unitários da funcionalidade.
- **Sequência obrigatória**: **RED → GREEN → REFACTOR**.
	- **RED**: escreva os testes e valide que falham pelo motivo correto.
	- **GREEN**: implemente o mínimo necessário para passar os testes.
	- **REFACTOR**: melhore o código mantendo os testes verdes.
- **Proibido implementar funcionalidade sem suíte de testes correspondente já criada**.
- **Escopo padrão**: testes unitários (sem testes de integração), salvo solicitação explícita.

### Linguagem e Nomenclatura
- **Inglês**: Nomes de pacotes, classes e métodos
- **Tradução Contextual**: Input português → inglês considerando domínio

### Identificadores
- **UUID**: Padrão para todas as entidades e DTOs
- **Tipo**: `java.util.UUID`
- **Características**: Únicos e imutáveis

### Mapeamento de Objetos
- **AutoMapper Obrigatório**: Use `modelMapper.map()` diretamente para todos os casos
- **Exceção**: Criação de entidades deve usar factory methods (regras de negócio)
- **Configurações Complexas**: Utilize a pasta `**/mapper/` apenas quando os campos entre os objetos forem diferentes
- **Sem Classes e métodos Auxiliares**: Não crie classes ou métodos auxiliares de mapeamento como por exemplo `mapToInputDto()`, `mapToResponse()`, etc.

### DTOs e Estrutura
- **Input DTOs**: Para entrada de dados
- **Output DTOs**: Para saída de dados
- **Lombok**: Obrigatório para DTOs (sem construtores/getters manuais)

## Sequência de Implementação

### 1. Core Domain
- Entidades de domínio
- Value objects e enums  
- Domain services
- **Instruções especializadas:** [`Domain Instructions`](domain.instructions.md)

### 2. Core Ports & Application
- Interfaces de adaptadores de Entrada (PORT IN)
- Interfaces de adaptadores de saída (PORT OUT)
- DTOs input/output
- Services de aplicação
- **Instruções especializadas:** [`Application Instructions`](application.instructions.md)
- **Instruções especializadas:** [`Ports Instructions`](ports.instructions.md)

### 3. API
- Controllers REST
- Validações
- **Instruções especializadas:** [`API Instructions`](api.instructions.md)

### 4. Infrastructure
- Implementação dos ports out
- Repositories JPA
- Clientes externos
- **Instruções especializadas:** [`Infrastructure Instructions`](infrastructure.instructions.md)

### 5. Observabilidade (Cross-Cutting)
- Logging com `@Slf4j` em todas as camadas
- Métricas de negócio com `MeterRegistry`
- Health checks customizados quando necessário
- **Instruções especializadas:** [`Observability Instructions`](observability.instructions.md)

## Testes
Implemente apenas Testes unitários, não é necessário implementar testes de integração.

Implemente testes unitários para:
- **Core**: Testes de lógica de negócio
- **Application**: Testes de serviços de aplicação
- **API**: Testes de controllers com dependências mockadas
- **Infrastructure**: Testes unitários de adapters/repositories com dependências mockadas

## 📚 Bibliotecas e Aceleradores

### Bibliotecas Telefônica
- **Exception Handler**: Já temos um handler global configurado
- **Log**: Padrões de logging corporativo
- **Segurança**: Autenticação e autorização
- **Shared**: Utilitários compartilhados