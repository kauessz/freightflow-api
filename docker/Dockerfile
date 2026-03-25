# ==================== Stage 1: Build ====================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar apenas o pom.xml primeiro para cachear dependências
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw

# Baixar dependências (camada cacheada)
RUN ./mvnw dependency:go-offline -B

# Copiar código-fonte e compilar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ==================== Stage 2: Runtime ====================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S freightflow && adduser -S freightflow -G freightflow

WORKDIR /app

# Copiar apenas o JAR gerado
COPY --from=builder /app/target/*.jar app.jar

# Usuário não-root
USER freightflow

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
