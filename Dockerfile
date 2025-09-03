# Dockerfile
# STAGE 1: Builder
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle/
COPY settings.gradle .
COPY build.gradle .
COPY backend/build.gradle ./backend/
RUN ./gradlew dependencies
COPY backend ./backend
RUN ./gradlew :backend:build -x test

# STAGE 2: Runner
FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/backend/build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]