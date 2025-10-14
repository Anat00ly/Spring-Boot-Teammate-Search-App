# ЗАМЕНИ содержимое Dockerfile на это:
FROM openjdk:21-jdk-slim

WORKDIR /app

# Копируем правильное имя JAR файла
COPY target/SpringBoot-0.0.1-SNAPSHOT.jar app.jar

# Создаем директорию для загрузок
RUN mkdir -p /app/uploads/avatar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]