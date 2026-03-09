---
applyTo: '**/api/rest/**'
---

## 🎯 Responsabilidades

Controllers são **adaptadores de entrada** que recebem requisições HTTP e as direcionam para o Core da aplicação, sem conter lógica de negócio.

### Core Responsibilities
- **Receber requisições HTTP** e validar entrada básica
- **Delegar para Input Ports** (use cases)
- **Retornar responses adequados** com status codes corretos
- **NÃO conter lógica de negócio**

## 🌐 Padrões REST Obrigatórios

### URI Design
- **Plural nouns**: `/customers`, `/orders`
- **Hierarquia**: `/customers/{id}/orders`
- **kebab-case**: `/order-items`, `/customer-profiles`
- **actions-cas**: `/carts/{id}/place-order`
- **No verbs**: Evitar verbos nas URIs, usar métodos HTTP para ações
- **No query parameters**: Evitar parâmetros de consulta para ações principais, usar URIs para recursos
- **Versionamento**: `/api/v1/customers`

### Status Codes
- **200**: Success with data (GET, PUT)
- **201**: Created (POST) - incluir Location header
- **204**: Success without data (DELETE)
- **400**: Bad request (validation error)
- **401**: Unauthenticated
- **403**: Unauthorized (forbidden)
- **404**: Not found
- **422**: Unprocessable entity (business validation)

## 🏗 Implementation Pattern

### TDD Obrigatório para API
- Antes de implementar controller/interface REST, escreva primeiro os testes unitários do endpoint (com dependências mockadas).
- Os testes devem cobrir contrato HTTP mínimo: status code, payload de resposta e validações de entrada.
- Siga o ciclo **RED → GREEN → REFACTOR** sem pular a etapa RED.
- Não criar código de produção de endpoint sem suíte de teste correspondente.

### 1. Interface com OpenAPI Annotations
Adicione todas as anotações OpenAPI na interface do controller, incluindo `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`, etc.

### 2. Implementação com Lógica do Controller
Implemente a interface em uma classe anotada com `@RestController`, `@RequestMapping`, e outras anotações Spring.

## 📊 Observabilidade em Controllers

### Obrigatório
- **`@Slf4j`** em todo controller
- **`log.debug`** na entrada do método com dados da requisição (sem dados sensíveis)
- **`log.info`** após operação bem-sucedida com o ID do recurso criado/modificado
- **Nunca** logar passwords, tokens, dados de cartão

### Exemplo
```java
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController implements CustomerControllerApi {

    @Override
    public ResponseEntity<CustomerOutputDto> create(@Valid @RequestBody CreateCustomerInputDto input) {
        log.debug("Received create customer request: {}", input.getEmail());
        CustomerOutputDto output = createCustomerUseCase.execute(input);
        log.info("Customer created: {}", output.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }
}
```

Para regras completas de observabilidade, consulte [`Observability Instructions`](observability.instructions.md).