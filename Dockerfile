# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml first for dependency caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy source and build
COPY src ./src
RUN ./mvnw -DskipTests package -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd -g 1000 appuser && \
    useradd -u 1000 -g appuser -m -s /bin/bash appuser

# Create data directory for SQLite database
RUN mkdir -p /app/data && chown -R appuser:appuser /app/data

# Copy JAR from build stage
COPY --from=builder /build/target/*.jar /app/app.jar
RUN chown appuser:appuser /app/app.jar

# Environment variables with defaults
ENV DB_PATH=/app/data/haushaltsbuch.db
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

# Expose application port
EXPOSE 8080

# Volume for SQLite database persistence
VOLUME /app/data

# Run as non-root user
USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
