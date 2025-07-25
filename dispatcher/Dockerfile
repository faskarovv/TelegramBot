#FROM openjdk:17-jdk-slim
#
#ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m"
#
#WORKDIR /app
#
#COPY  build/libs/dispatcher-1.0-SNAPSHOT.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]
#
#EXPOSE 8081

# -------- Stage 1: Build --------
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# -------- Stage 2: Run --------
FROM openjdk:17-jdk-slim
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m"
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

