FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -q -e dependency:go-offline

COPY src ./src

RUN mvn -q -e -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=deploy

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
