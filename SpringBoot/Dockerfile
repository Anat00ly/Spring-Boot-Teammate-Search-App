FROM openjdk:17-jdk-slim-buster
WORKDIR /app
COPY target/SpringBoot-0.0.1-SNAPSHOT.jar /app/application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]