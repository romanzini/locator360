---
applyTo: '**/core/application/**'
---

# Application Services - Implementação Detalhada

## 🎯 Responsabilidades

A camada Application atua como **orquestradora** dos casos de uso, coordenando operações entre Domain e Infrastructure sem conter regras de negócio próprias.

### Core Responsibilities
- **Implementar casos de uso** definidos nos Ports IN
- **Coordenar operações** entre Domain e Infrastructure
- **Gerenciar transações** (boundaries)
- **Mapear DTOs** para objetos de domínio e vice-versa

## ⚠️ Regras Obrigatórias

### TDD Obrigatório para Application
- Antes de implementar qualquer service/use case, crie primeiro os testes unitários do caso de uso.
- Os testes devem iniciar em estado **RED** (falhando pelo motivo esperado) e somente depois a implementação é permitida.
- A implementação deve ser incremental, escrevendo apenas o mínimo para passar os testes (**GREEN**).
- Refatorações são permitidas apenas com a suíte verde e sem reduzir cobertura da funcionalidade.

### Method Guidelines
- **Um método público** por caso de uso
- **Early return pattern** para validações
- **Fail-fast validation** na entrada

### Spring Annotations
- **@Service**: Marca como serviço Spring
- **@RequiredArgsConstructor**: Para injeção de dependência via construtor

### Observabilidade Obrigatória
- **`@Slf4j`** em todo Application Service
- **`log.debug`** na entrada do método com parâmetros relevantes
- **`log.info`** ao concluir a operação de negócio com sucesso
- **`log.error`** em blocos catch com a exceção como último parâmetro
- **Métricas de negócio**: Injetar `MeterRegistry` e registrar counters/timers para operações críticas
- Consulte [`Observability Instructions`](observability.instructions.md) para regras completas

### Clean Code and SOLID Practices
- **Nomes claros**: Use nomes descritivos para métodos e variáveis

## 🔄 Mapping Strategies (CRÍTICO)

### AutoMapper Obrigatório - USO DIRETO
- **SEMPRE use ModelMapper DIRETO**: `modelMapper.map(source, Target.class)` no service
- **NÃO crie classes auxiliares de mapper**: Injete `ModelMapper` e use diretamente
- **Configurações específicas**: Pasta `mapper/` apenas para configurações complexas quando necessário
- **Criação de entidades**: Use factory methods (preserva regras de negócio)

### Padrão Correto - Uso Direto nos Services
```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerService implements CustomerUseCase {
    
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper; // ✅ Injeção direta
    
    @Override
    public CustomerOutputDto createCustomer(CreateCustomerInputDto input) {
        // ✅ CORRETO: Factory method para criação com regras de negócio
        Customer customer = Customer.create(input.getName(), input.getEmail());
        
        Customer savedCustomer = customerRepository.save(customer);
        
        // ✅ CORRETO: ModelMapper direto no service
        return modelMapper.map(savedCustomer, CustomerOutputDto.class);
    }
    
    // ✅ CORRETO: Para listas também use direto
    public List<CustomerOutputDto> findAll() {
        return customerRepository.findAll().stream()
            .map(customer -> modelMapper.map(customer, CustomerOutputDto.class))
            .collect(Collectors.toList());
    }
}
```

### Configuração ModelMapper Personalizada (core/application/mapper/)
Apenas quando necessário para mapeamentos complexos:
```java
@Configuration
public class CustomerMapperConfig {
    
    @Bean
    @Qualifier("customerModelMapper")
    public ModelMapper customerModelMapper() {
        ModelMapper mapper = new ModelMapper();
        
        // Configuração específica para Customer - APENAS quando campos têm nomes diferentes
        mapper.createTypeMap(CreateCustomerInputDto.class, Customer.class)
            .addMappings(mapping -> {
                mapping.skip(Customer::getId); // ID será gerado na factory
                mapping.skip(Customer::getCreatedAt); // Data será gerada na factory
            });
            
        return mapper;
    }
}
```

## 🔄 AutoMapper (OBRIGATÓRIO)

### ⚠️ REGRAS RIGOROSAS - NUNCA VIOLE
- **USE DIRETO**: `modelMapper.map(source, TargetClass.class)` no código
- **❌ PROIBIDO**: Métodos auxiliares como `mapToEntity()`, `mapToDomain()`, `mapToResponseDto()`, etc.
- **✅ CORRETO**: `.map(entity -> modelMapper.map(entity, Domain.class))`
- **Configurações**: Pasta `mapper/` apenas para campos com nomes diferentes
- **Criação de entidades**: Use factory methods (preserva regras de negócio)

### ❌ EXEMPLO INCORRETO (NÃO FAÇA)
```java
@Service
public class CustomerService {
    private final ModelMapper modelMapper;
    
    public CustomerDto save(CustomerDto dto) {
        Customer entity = mapToEntity(dto);  // ❌ ERRADO
        return mapToDto(entity);             // ❌ ERRADO
    }
    
    private Customer mapToEntity(CustomerDto dto) {    // ❌ PROIBIDO
        return modelMapper.map(dto, Customer.class);
    }
    
    private CustomerDto mapToDto(Customer entity) {    // ❌ PROIBIDO
        return modelMapper.map(entity, CustomerDto.class);
    }
}
```

### ✅ EXEMPLO CORRETO (SEMPRE FAÇA)
```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreateCustomerService implements CreateCustomerUseCase {
    
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public CreateCustomerOutputDto execute(CreateCustomerInputDto input) {
        // Factory method para criação (regras de negócio)
        Customer customer = Customer.create(input.getName(), input.getEmail(), input.getDocument());
        
        Customer savedCustomer = customerRepository.save(customer);
        
        // ✅ CORRETO: Use modelMapper diretamente
        return modelMapper.map(savedCustomer, CreateCustomerOutputDto.class);
    }
}
```

### Configuração Complexa (mapper/)
Apenas quando nomes de campos são diferentes:
```java
@Component
public class CustomerMapperConfig {
    
    @Autowired
    public void configureCustomerMappings(ModelMapper modelMapper) {
        // APENAS quando campos têm nomes diferentes
        modelMapper.typeMap(Customer.class, CustomerOutputDto.class)
            .addMapping(Customer::getId, CustomerOutputDto::setCustomerId)
            .addMapping(src -> src.getEmail().getValue(), CustomerOutputDto::setEmailAddress);
    }
}
```

## 🚨 Exception Handling

### Translation Pattern
```java
@Override
public CustomerOutputDto execute(CreateCustomerInputDto input) {
    try {
        // Application logic...
        return result;
    } catch (DatabaseException ex) {
        // Translate infrastructure exception to domain exception
        throw new CustomerPersistenceException("Failed to save customer", ex);
    }
}
```

## 📁 Estrutura Recomendada

```text
core/application/
├─ service/
│  ├─ CreateCustomerService.java
│  ├─ UpdateCustomerService.java
│  ├─ FindCustomerService.java
│  └─ DeleteCustomerService.java
└─ mapper/                              # ← OPCIONAL: Apenas para configurações complexas como nomes diferentes de campos
   ├─ CustomerMapperConfig.java      # ← Apenas se necessário
   └─ OrderMapperConfig.java         # ← Apenas se necessário
```

**IMPORTANTE:** A pasta `mapper/` NÃO é necessária. Use ModelMapper diretamente nos services.

## 🎯 Testing Guidelines

- **TDD obrigatório**: testes primeiro, implementação depois
- **Unit tests apenas** (não criar testes de integração conforme instrução geral)
- **Mock dependencies** (repositories, external services)
- **Test business flows** end-to-end no service
- **Verify interactions** com mocks

---

> 🔗 **Próximos passos**: Continue com `api-implementation.md` para implementar os controllers