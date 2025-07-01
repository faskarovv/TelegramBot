FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/node-1.0-SNAPSHOT-plain.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8082
