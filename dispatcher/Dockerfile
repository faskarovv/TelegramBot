FROM openjdk:17-jdk-slim

WORKDIR /app

COPY  build/libs/dispatcher-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8081



