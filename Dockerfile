# STAGE 1: Builder
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle/
COPY settings.gradle .
COPY backend/build.gradle ./backend/

RUN chmod +x ./gradlew

RUN ./gradlew dependencies
COPY backend ./backend
RUN ./gradlew :backend:build -x test

# STAGE 2: Runner
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/backend/build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]