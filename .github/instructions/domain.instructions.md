---
applyTo: '**/core/domain/**'
---

# Domain Layer - Implementação Detalhada

> 📖 **Pré-requisito**: Leia `vexa-architecture.md` para contexto completo da arquitetura.

## 🎯 Responsabilidades

A camada Domain representa o **coração da aplicação**, onde todas as regras de negócio são definidas e executadas. Esta camada é responsável por encapsular a lógica central que controla como a aplicação funciona, independentemente de detalhes técnicos.

## 🏗 Componentes

### Entities (Domain Model)
- **Business logic**: Não apenas getters/setters (evite Lombok @Data)
- **Constructor validation**: Valida invariantes no construtor
- **Immutability**: Imutável quando possível
- **Rich domain model**: Comportamentos ricos no domínio
- **Factory methods**: Métodos estáticos para criação

### Value Objects (core/domain/vo/)
- Objetos imutáveis que representam conceitos do domínio
- Sem identidade própria
- Validação no construtor
- Equals/HashCode baseado em valores

### Domain Services (core/domain/service/)
- Lógicas complexas que envolvem múltiplas entidades
- Operações que não pertencem a uma única entidade
- Regras de negócio de alto nível

## ⚠️ Regras Obrigatórias

- **Zero dependencies**: Sem dependências de frameworks
- **Zero infrastructure**: Sem preocupações de infraestrutura  
- **Pure business logic**: Apenas lógica de negócio
- **Domain exceptions**: Use exceções específicas do domínio
- **English naming**: Nomes em inglês sempre

### TDD Obrigatório para Domain
- Antes de escrever qualquer entidade, value object ou domain service, escreva primeiro os testes unitários da regra de negócio.
- Valide o ciclo **RED → GREEN → REFACTOR** para cada comportamento do domínio.
- Não implemente código de produção no Domain sem teste correspondente já criado.

## 🔄 Domain e AutoMapper

### Regras para Domain
- **Domain permanece puro**: Não usar bibliotecas de mapeamento no Domain
- **Factory methods obrigatórios**: Para preservar regras de negócio na criação
- **AutoMapper nas bordas**: Mapeamento acontece em Application e Infrastructure

### Factory Pattern (Obrigatório)
```java
public class Customer {
    // Factory para criação (preserva regras de negócio)
    public static Customer create(String name, String email, String document) {
        validateBusinessRules(name, email, document);
        return new Customer(UUID.randomUUID(), name, Email.of(email), Document.of(document));
    }
    
    // Factory para reconstituição (vindo do repository)
    public static Customer restore(UUID id, String name, String email, String document) {
        return new Customer(id, name, Email.of(email), Document.of(document));
    }
    
    private static void validateBusinessRules(String name, String email, String document) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCustomerException("Customer name is required");
        }
    }
}
```

## 📝 Exemplo de Estrutura

```java
// Entity Example
public class Customer {
    private final UUID id;
    private final String name;
    private final Email email;
    
    private Customer(UUID id, String name, Email email) {
        this.id = requireNonNull(id);
        this.name = validateName(name);
        this.email = requireNonNull(email);
    }
    
    public static Customer create(String name, String email) {
        return new Customer(UUID.randomUUID(), name, Email.of(email));
    }
    
    // Business methods here...
}
```

---

> 🔗 **Próximos passos**: Após implementar Domain, continue com `ports-implementation.md`