# Stage 1: Build the application
FROM maven:3.8.4-openjdk-11 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/transaction-service-*.jar app.jar
COPY ../scripts/startup.sh /app/startup.sh

RUN chmod +x /app/startup.sh

EXPOSE 8080

ENTRYPOINT ["/app/startup.sh"]
