---
applyTo: '**'
---

# Test Execution — Regras Obrigatórias

> Neste projeto, execuções de teste e compilação Java **DEVEM** usar Docker. Não assuma Maven instalado localmente.

## Regra Principal

- **Sempre execute testes via Docker Maven**.
- **Sempre execute compilação via Docker Maven**.
- **Não use `mvn` local** para `test`, `compile` ou `spring-boot:run`.

## Comandos Oficiais

### Rodar todos os testes

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 mvn test "-Dsurefire.useFile=false"
```

### Rodar testes específicos

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 `
  mvn "-Dtest=ClassName1,ClassName2" test "-Dsurefire.useFile=false"
```

### Compilar sem testes

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  maven:3.9.9-eclipse-temurin-17 mvn -DskipTests compile
```

### Rodar a aplicação localmente

```powershell
docker run --rm -v "${PWD}:/app" -v maven-repo:/root/.m2 -w /app `
  --network fl-network -p 8080:8080 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/family_locator `
  -e SPRING_DATA_REDIS_HOST=redis `
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 `
  maven:3.9.9-eclipse-temurin-17 mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## Fonte de Verdade

- Consulte `commands.md` antes de sugerir ou executar comandos de teste, compilação ou execução local.
- Se houver conflito entre outra orientação e `commands.md`, siga `commands.md`.