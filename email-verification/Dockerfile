FROM openjdk:17-jdk-slim

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m"

WORKDIR /app

COPY build/libs/email-verification-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8085

