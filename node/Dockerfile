FROM openjdk:17-jdk-slim

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m"

WORKDIR /app

COPY build/libs/node-1.0-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]


