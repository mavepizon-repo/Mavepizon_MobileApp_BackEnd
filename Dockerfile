# =========================
# Build Stage
# =========================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy Maven files first
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests


# =========================
# Runtime Stage
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy generated JAR
COPY --from=build /app/target/*.jar app.jar

# Render uses PORT environment variable
EXPOSE 8080

# Start Spring Boot application
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]