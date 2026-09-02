# =========================
# Build Stage
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first for Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package application
RUN mvn clean package -DskipTests


# =========================
# Runtime Stage
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the generated JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot application port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]