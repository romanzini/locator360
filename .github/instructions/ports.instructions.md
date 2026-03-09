---
applyTo: '**/core/port/**'
---

# Ports (Interfaces) - Implementação Detalhada

> 📖 **Pré-requisito**: Leia `vexa-architecture.md` para contexto completo da arquitetura.

## 🎯 Conceito de Ports

Os **Ports** são as **"portas"** da Arquitetura Hexagonal - interfaces bem definidas que permitem ao Core interagir com o mundo exterior sem conhecer detalhes de implementação.

## 🧪 TDD Obrigatório para Ports
- Defina primeiro os cenários de teste da funcionalidade (casos de uso esperados e contratos de saída/entrada) antes de implementar classes concretas.
- Quando houver DTOs e contratos de use case novos, os testes dos serviços que consomem esses ports devem ser criados antes da implementação.
- Siga o fluxo **RED → GREEN → REFACTOR** no desenvolvimento orientado aos contracts dos ports.

## 🔌 Input Ports (core/port/in/)

### Responsabilidades
- **Definir assinaturas** dos casos de uso
- **Contratos de entrada** para a camada API
- **Input DTOs** como parâmetros
- **Output DTOs** ou objetos de domínio como retorno

### Padrões Obrigatórios
```java
// Use case interface
public interface CreateCustomerUseCase {
    CreateCustomerOutputDto execute(CreateCustomerInputDto input);
}
```

## 🔌 Output Ports (core/port/out/)

### Responsabilidades
- **Contratos de infraestrutura** para sistemas externos
- **Repository interfaces** para persistência
- **External service interfaces** para APIs externas
- **Event publishing interfaces** para mensageria

### Padrões Obrigatórios
```java
// Repository interface
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    List<Customer> findByEmail(String email);
}

// External service interface
public interface EmailService {
    void sendWelcomeEmail(String email, String name);
}
```

## 📋 DTO Rules (CRÍTICO)

### ⚠️ Regras Obrigatórias para DTOs
- **APENAS Lombok annotations** - sem construtores, getters ou setters manuais
- **Validations permitidas**: @NotBlank, @NotNull, @Valid, etc.
- **Foco em transferência**: Manter DTOs simples e focados
- **Separação clara**: Input vs Output DTOs

### ✅ Exemplo Correto
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerInputDto {
    @NotBlank
    private String name;
    
    @Email
    @NotBlank
    private String email;
    
    @NotNull
    private UUID companyId;
}

@Data
@Builder
public class CreateCustomerOutputDto {
    private UUID id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
```

### ❌ Exemplo Incorreto
```java
public class CreateCustomerInputDto {
    private String name;
    
    // ❌ NÃO faça isso - use Lombok
    public CreateCustomerInputDto(String name) {
        this.name = name;
    }
    
    // ❌ NÃO faça isso - use Lombok
    public String getName() {
        return name;
    }
}
```

## 🔄 DTOs e AutoMapper

### DTOs nos Ports
- **Contracts simples**: DTOs são apenas contratos de dados
- **AutoMapper friendly**: Estrutura compatível com mapeamento automático
- **Records preferidos**: Use records para imutabilidade

### Input/Output DTOs
```java
// Input DTO
public record CreateCustomerInputDto(
    @NotBlank String name,
    @Email String email,
    @NotBlank String document
) {}

// Output DTO
@Builder
@Value
public class CustomerOutputDto {
    UUID id;
    String name;
    String email;
    String document;
    LocalDateTime createdAt;
}
```

### Repository Ports
```java
// Repository trabalha com objetos de Domain
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Page<Customer> findAll(Pageable pageable);
}
```

## 🎯 Design Patterns

### Interface Segregation Principle
- **Interfaces pequenas** e focadas
- **Uma responsabilidade** por interface
- **Evite interfaces grandes** e genéricas

### Domain-Centric Naming
- **Nomes do domínio**: Use linguagem ubíqua
- **Sem detalhes técnicos**: Evite nomes como DatabaseRepository
- **Expressivo**: CreateCustomerUseCase vs CustomerService

### Exemplo de Estrutura
```text
core/port/
├─ in/
│  ├─ CreateCustomerUseCase.java
│  ├─ FindCustomerUseCase.java
│  ├─ UpdateCustomerUseCase.java
│  └─ dto/
│     ├─ input/
│     │  ├─ CreateCustomerInputDto.java
│     │  └─ UpdateCustomerInputDto.java
│     └─ output/
│        ├─ CustomerOutputDto.java
│        └─ CustomerSummaryOutputDto.java
└─ out/
   ├─ CustomerRepository.java
   ├─ EmailService.java
   └─ CustomerEventPublisher.java
```

---

> 🔗 **Próximos passos**: Continue com `application-implementation.md` para implementar os use cases