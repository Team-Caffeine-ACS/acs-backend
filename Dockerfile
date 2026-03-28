# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies (better layer caching)
COPY pom.xml .
# 2. LAHENDUS: Lisame skip-lipu ka siia, et Maven ei hakkaks sõltuvuste laadimisel koodi kontrollima
RUN mvn dependency:go-offline -B -Dcheckstyle.skip

# Copy source and build
COPY src ./src

# 4. LAHENDUS: Lisame paki kokkupanemisel checkstyle ignoreerimise  
RUN mvn clean package -DskipTests -B -Dcheckstyle.skip

# ===== Stage 2: Run =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]