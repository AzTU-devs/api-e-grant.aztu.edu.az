# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src/ src/
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
