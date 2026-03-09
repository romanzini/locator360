---
applyTo: '**/infrastructure/**'
---

# Infrastructure Layer - Implementação Detalhada

> 📖 **Pré-requisito**: Leia `vexa-architecture.md` para contexto completo da arquitetura.

## 🎯 Responsabilidades

A camada Infrastructure implementa os **Output Ports** definidos no Core, fornecendo acesso concreto a sistemas externos como bancos de dados, APIs e serviços de mensageria.

### Core Responsibilities
- **Implementar Output Ports** (interfaces de saída)
- **Traduzir exceções** de infraestrutura para exceções de domínio
- **Gerenciar configurações** específicas de tecnologia
- **NÃO conter lógica de negócio**

## 🧪 TDD Obrigatório para Infrastructure
- Antes de implementar adapters/repositories/publishers, escreva os testes unitários da funcionalidade com dependências mockadas.
- Valide primeiro o estado **RED** e só então implemente o mínimo para chegar ao **GREEN**.
- Não adicionar código de produção na Infrastructure sem teste correspondente previamente criado.

## 🗄 Repositories (infrastructure/persistence/)

### JPA Implementation Pattern
```java
@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomerJpaRepository implements CustomerRepository {
    
    private final SpringDataCustomerRepository springRepository;
    private final CustomerEntityMapper mapper; // AutoMapper injection
    
    @Override
    public Customer save(Customer customer) {
        log.debug("Saving customer: {}", customer.getId());
        
        // Use AutoMapper ao invés de cast manual
        CustomerEntity entity = mapper.toEntity(customer);
        CustomerEntity savedEntity = springRepository.save(entity);
        
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Customer> findById(UUID id) {
        log.debug("Finding customer by id: {}", id);
        
        return springRepository.findById(id)
            .map(mapper::toDomain); // Method reference com AutoMapper
    }
    
    @Override
    public List<Customer> findByEmail(String email) {
        log.debug("Finding customers by email: {}", email);
        
        return springRepository.findByEmail(email)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}

// Spring Data Interface
interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    List<CustomerEntity> findByEmail(String email);
}
```

### JPA Entity Example
```java
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Entity-Domain Mapper (OPCIONAL: Apenas para configurações complexas)
**Padrão recomendado: USO DIRETO do ModelMapper nos repositories**

```java
@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomerJpaRepository implements CustomerRepository {
    
    private final SpringDataCustomerRepository springRepository;
    private final ModelMapper modelMapper; // ✅ Injeção direta
    
    @Override
    public Customer save(Customer customer) {
        log.debug("Saving customer: {}", customer.getId());
        
        // ✅ CORRETO: Use ModelMapper direto
        CustomerEntity entity = modelMapper.map(customer, CustomerEntity.class);
        CustomerEntity savedEntity = springRepository.save(entity);
        
        return modelMapper.map(savedEntity, Customer.class);
    }
    
    @Override
    public Optional<Customer> findById(UUID id) {
        log.debug("Finding customer by id: {}", id);
        
        return springRepository.findById(id)
            .map(entity -> modelMapper.map(entity, Customer.class));
    }
    
    @Override
    public Page<Customer> findAll(Pageable pageable) {
        Page<CustomerEntity> entityPage = springRepository.findAll(pageable);
        return entityPage.map(entity -> modelMapper.map(entity, Customer.class));
    }
}
```

### Configuração AutoMapper Personalizada (infrastructure/persistence/mapper/)
**OPCIONAL: Apenas quando necessário para mapeamentos complexos como nomes diferentes de campos**

```java
@Configuration
public class CustomerEntityMapperConfig {
    
    @Bean
    @Qualifier("customerEntityModelMapper")
    public ModelMapper customerEntityModelMapper() {
        ModelMapper mapper = new ModelMapper();
        
        // APENAS quando Entity e Domain têm campos com nomes diferentes
        mapper.createTypeMap(CustomerEntity.class, Customer.class)
            .setPostConverter(context -> {
                CustomerEntity source = context.getSource();
                return Customer.restore(
                    source.getId(),
                    source.getName(),
                    source.getEmail(),
                    source.getCreatedAt()
                );
            });
            
        return mapper;
    }
}
```

## 🌐 External Clients (infrastructure/rest/, infrastructure/soap/)

### REST Client Implementation (AutoMapper)
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceClient implements EmailService {
    
    private final RestTemplate restTemplate;
    private final EmailServiceProperties properties;
    private final ModelMapper modelMapper;  // ← AutoMapper
    
    @Override
    public void sendWelcomeEmail(String email, String name) {
        log.debug("Sending welcome email to: {}", email);
        
        try {
            // ✅ CORRETO: Use AutoMapper para external requests
            WelcomeEmailData emailData = WelcomeEmailData.builder()
                .email(email)
                .name(name)
                .build();
                
            EmailRequest request = modelMapper.map(emailData, EmailRequest.class);
            request.setSubject("Welcome!");
            request.setBody(buildWelcomeMessage(name));
                
            EmailResponse response = restTemplate.postForObject(
                properties.getBaseUrl() + "/emails/send",
                request,
                EmailResponse.class
            );
            
            // Log response if needed
            log.debug("Email sent successfully: {}", response);
            
        } catch (RestClientException ex) {
            log.error("Failed to send email to: {}", email, ex);
            throw new EmailDeliveryException("Failed to send welcome email", ex);
        }
    }
    
    private String buildWelcomeMessage(String name) {
        return String.format("Welcome %s! Thanks for joining us.", name);
    }
}

// DTO for internal use
@Data
@Builder
public class WelcomeEmailData {
    private String email;
    private String name;
}
```

### Configuration Properties
```java
@ConfigurationProperties(prefix = "email-service")
@Data
public class EmailServiceProperties {
    private String baseUrl;
    private Duration timeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
}
```

### REST Template Configuration
```java
@Configuration
@EnableConfigurationProperties(EmailServiceProperties.class)
public class RestClientConfig {
    
    @Bean
    public RestTemplate restTemplate(EmailServiceProperties properties) {
        return new RestTemplateBuilder()
            .setConnectTimeout(properties.getTimeout())
            .setReadTimeout(properties.getTimeout())
            .errorHandler(new CustomResponseErrorHandler())
            .build();
    }
}
```

## 📨 Event Publishers (infrastructure/event/)

### Kafka Publisher Implementation (AutoMapper)
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEventKafkaPublisher implements CustomerEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CustomerEventProperties properties;
    private final ModelMapper modelMapper;  // ← AutoMapper
    
    @Override
    public void publishCustomerCreated(Customer customer) {
        log.debug("Publishing customer created event: {}", customer.getId());
        
        try {
            // ✅ CORRETO: Use AutoMapper para eventos
            CustomerCreatedEvent event = modelMapper.map(customer, CustomerCreatedEvent.class);
            event.setTimestamp(Instant.now());
            
            kafkaTemplate.send(properties.getCustomerTopic(), customer.getId().toString(), event)
                .addCallback(
                    result -> log.debug("Event published successfully: {}", customer.getId()),
                    failure -> log.error("Failed to publish event: {}", customer.getId(), failure)
                );
                
        } catch (Exception ex) {
            log.error("Error publishing customer created event: {}", customer.getId(), ex);
            throw new EventPublishingException("Failed to publish customer created event", ex);
        }
    }
}

// Configuração específica para eventos (infrastructure/event/kafka/mapper/)
@Configuration
public class CustomerEventMapperConfig {
    
    @Bean
    @Qualifier("customerEventModelMapper")
    public ModelMapper customerEventModelMapper() {
        ModelMapper mapper = new ModelMapper();
        
        mapper.createTypeMap(Customer.class, CustomerCreatedEvent.class)
            .addMappings(mapping -> {
                mapping.map(Customer::getId, CustomerCreatedEvent::setCustomerId);
                mapping.map(Customer::getName, CustomerCreatedEvent::setCustomerName);
                mapping.map(src -> src.getEmail().getValue(), CustomerCreatedEvent::setCustomerEmail);
                mapping.skip(CustomerCreatedEvent::setTimestamp); // Será setado manualmente
            });
            
        return mapper;
    }
}
```

## 🔄 AutoMapper na Infrastructure

### ⚠️ REGRAS RIGOROSAS - NUNCA VIOLE
- **Repository**: `modelMapper.map()` DIRETO entre Entity ↔ Domain
- **External APIs**: `modelMapper.map()` DIRETO para requests/responses  
- **❌ PROIBIDO**: Métodos auxiliares como `mapToEntity()`, `mapToDomain()`, `mapToRequest()`, etc.
- **✅ CORRETO**: `.map(entity -> modelMapper.map(entity, Domain.class))`
- **NÃO criar classes auxiliares**: Injete ModelMapper e use diretamente
- **Configurações**: Pasta `mapper/` APENAS para campos com nomes diferentes

### ❌ EXEMPLO INCORRETO (NÃO FAÇA)
```java
@Repository
public class CustomerJpaRepository implements CustomerRepository {
    private final ModelMapper modelMapper;
    
    public Customer save(Customer customer) {
        CustomerEntity entity = mapToEntity(customer);  // ❌ ERRADO
        return mapToDomain(entity);                     // ❌ ERRADO
    }
    
    public Optional<Customer> findById(UUID id) {
        return springRepository.findById(id)
            .map(this::mapToDomain);  // ❌ ERRADO
    }
    
    private CustomerEntity mapToEntity(Customer customer) {  // ❌ PROIBIDO
        return modelMapper.map(customer, CustomerEntity.class);
    }
    
    private Customer mapToDomain(CustomerEntity entity) {    // ❌ PROIBIDO
        return modelMapper.map(entity, Customer.class);
    }
}
```

### ✅ EXEMPLO CORRETO (SEMPRE FAÇA)
```java
@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomerJpaRepository implements CustomerRepository {
    
    private final SpringDataCustomerRepository springRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public Customer save(Customer customer) {
        // ✅ CORRETO: Use modelMapper diretamente
        CustomerEntity entity = modelMapper.map(customer, CustomerEntity.class);
        CustomerEntity savedEntity = springRepository.save(entity);
        return modelMapper.map(savedEntity, Customer.class);
    }
    
    @Override
    public Optional<Customer> findById(UUID id) {
        return springRepository.findById(id)
            // ✅ CORRETO: Lambda com modelMapper direto
            .map(entity -> modelMapper.map(entity, Customer.class));
    }
    
    @Override
    public Page<Customer> findAll(Pageable pageable) {
        Page<CustomerEntity> entityPage = springRepository.findAll(pageable);
        // ✅ CORRETO: Lambda com modelMapper direto
        return entityPage.map(entity -> modelMapper.map(entity, Customer.class));
    }
}
```

### External Client Exemplo
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceClient implements EmailService {
    
    private final RestTemplate restTemplate;
    private final ModelMapper modelMapper; // ✅ Injeção direta
    
    @Override
    public void sendWelcomeEmail(String email, String name) {
        WelcomeEmailData data = WelcomeEmailData.builder()
            .email(email)
            .name(name)
            .build();
            
        // ✅ CORRETO: Use ModelMapper direto
        EmailRequest request = modelMapper.map(data, EmailRequest.class);
        request.setSubject("Welcome!");
        
        restTemplate.postForObject("/emails/send", request, EmailResponse.class);
    }
}
```

### Configuração Complexa (mapper/)
**OPCIONAL: Apenas quando campos têm nomes diferentes**

```java
@Component
public class CustomerEntityMapperConfig {
    
    @Autowired
    public void configureEntityMappings(ModelMapper modelMapper) {
        // APENAS quando nomes são diferentes
        modelMapper.typeMap(CustomerEntity.class, Customer.class)
            .addMapping(CustomerEntity::getCustomerId, Customer::setId);
            
        modelMapper.typeMap(Customer.class, CustomerEntity.class)
            .addMapping(Customer::getId, CustomerEntity::setCustomerId)
            .skip(CustomerEntity::setCreatedAt); // JPA gerencia
    }
}
```

## ⚠️ Regras Obrigatórias

### Exception Handling and Translation
```java
@Override
public Customer save(Customer customer) {
    try {
        // Implementation...
        return result;
    } catch (DataIntegrityViolationException ex) {
        throw new CustomerAlreadyExistsException(customer.getEmail(), ex);
    } catch (DataAccessException ex) {
        throw new CustomerPersistenceException("Failed to save customer", ex);
    }
}
```

### Observabilidade Obrigatória
- **`@Slf4j`** em todo Repository, Client e Publisher
- **`log.debug`** na entrada e saída de operações de I/O
- **`log.error`** em blocos catch com a exceção como último parâmetro
- Para clientes externos: logar tempo de resposta e status em nível DEBUG
- Consulte [`Observability Instructions`](observability.instructions.md) para regras completas

### Technology-Specific Implementation Only
- **Sem lógica de negócio** na camada Infrastructure
- **Configurações externalizadas** via Properties
- **Timeout e retry** para clientes externos
- **Circuit breaker** para resiliência (quando aplicável)

## 📁 Estrutura Recomendada

```text
infrastructure/
├─ persistence/
│  ├─ postgresql/                    # Implementações JPA específicas
│  │  ├─ CustomerJpaRepository.java  # ← Use ModelMapper direto
│  │  ├─ entity/
│  │  │  └─ CustomerEntity.java
│  │  ├─ mapper/                        # ← OPCIONAL: Apenas para configurações complexas como nomes diferentes de campos
│  │  │  └─ CustomerEntityMapperConfig.java
│  │  └─ config/
│  │     └─ DatabaseConfig.java
│  └─ h2/                           # Para testes (se aplicável)
├─ rest/
│  ├─ EmailServiceClient.java       # ← Use ModelMapper direto
│  ├─ mapper/                          # ← OPCIONAL: Apenas para configurações complexas como nomes diferentes de campos
│  │  └─ EmailServiceMapperConfig.java
│  ├─ config/
│  │  └─ RestClientConfig.java
│  └─ properties/
│     └─ EmailServiceProperties.java
├─ soap/
│  ├─ LegacySystemClient.java       # ← Use ModelMapper direto
│  └─ mapper/                          # ← OPCIONAL: Apenas para configurações complexas como nomes diferentes de campos
└─ event/
   ├─ kafka/
   │  ├─ CustomerEventKafkaPublisher.java  # ← Use ModelMapper direto
   │  ├─ mapper/                       # ← OPCIONAL: Apenas para configurações complexas como nomes diferentes de campos
   │  │  └─ CustomerEventMapperConfig.java
   │  ├─ config/
   │  │  └─ KafkaConfig.java
   │  └─ properties/
   │     └─ CustomerEventProperties.java
   └─ rabbitmq/                     # Alternativa se necessário
```

**IMPORTANTE:** As pastas `mapper/` NÃO são necessárias. Use ModelMapper diretamente nos repositories e clients.

## 🎯 Configuration Patterns

### Database Configuration
```java
@Configuration
@EnableJpaRepositories(basePackages = "br.com.tlf.sfmn.infrastructure.persistence")
@EntityScan(basePackages = "br.com.tlf.sfmn.infrastructure.persistence.entity")
public class DatabaseConfig {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

---

> 🔗 **Finalização**: Sua arquitetura Vexa está completa! Consulte `patterns-and-examples.md` para padrões avançados e exemplos práticos.