FROM maven:3.8.6-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create directory for logs
RUN mkdir -p /var/log/hospital-queue
RUN chmod 777 /var/log/hospital-queue

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
