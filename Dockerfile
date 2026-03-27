# ==================== Stage 1: Build ====================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia pom.xml primeiro para cachear dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e builda sem testes
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==================== Stage 2: Runtime ====================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria usuário não-root para segurança
RUN addgroup -S freightflow && adduser -S freightflow -G freightflow

# Copia o JAR do stage de build
COPY --from=build /app/target/*.jar app.jar

# Ownership para o usuário não-root
RUN chown -R freightflow:freightflow /app
USER freightflow

# Porta exposta (Railway injeta PORT automaticamente)
EXPOSE 8080

# Health check via actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Configurações JVM otimizadas para containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
