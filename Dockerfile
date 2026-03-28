# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

ARG SKIP_CHECKSTYLE=false

# Copy pom.xml first and download dependencies (better layer caching)
COPY pom.xml .
RUN if [ "$SKIP_CHECKSTYLE" = "true" ]; then \
      mvn dependency:go-offline -B -Dcheckstyle.skip=true; \
    else \
      mvn dependency:go-offline -B; \
    fi

# Copy source and build
COPY src ./src

RUN if [ "$SKIP_CHECKSTYLE" = "true" ]; then \
      mvn clean package -DskipTests -B -Dcheckstyle.skip=true; \
    else \
      mvn clean package -DskipTests -B; \
    fi

# ===== Stage 2: Run =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]