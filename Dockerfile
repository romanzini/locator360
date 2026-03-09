# ============================================================
# Family Locator — Dockerfile (Multi-stage Build)
# ============================================================
# Stage 1: Build com Maven
# Stage 2: Runtime com JRE slim
# ============================================================

# --- Stage 1: Build ---
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copiar arquivos de configuração do Maven primeiro (cache de dependências)
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
RUN chmod +x mvnw

# Baixar dependências (camada cacheada)
RUN ./mvnw dependency:resolve -B -q 2>/dev/null || true

# Copiar código-fonte
COPY src/ src/

# Build do projeto (sem testes para agilizar o build de dev)
RUN ./mvnw package -DskipTests -B -q

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine AS runtime

# Metadados
LABEL maintainer="Family Locator Team"
LABEL description="Family Locator Backend API"

# Criar usuário não-root para segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar JAR do stage de build
COPY --from=builder /build/target/*.jar app.jar

# Permissões
RUN chown -R appuser:appgroup /app

# Trocar para usuário não-root
USER appuser

# Porta da aplicação
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entrypoint com suporte a JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar app.jar"]
