FROM openjdk:11

WORKDIR /app
COPY build/libs/BeachBunnyBot-0.1.0.jar .
COPY src/main/resources/token.json .

CMD ["java", "-jar", "/app/BeachBunnyBot-0.1.0.jar"]
