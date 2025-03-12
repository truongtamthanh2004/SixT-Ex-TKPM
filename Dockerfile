FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} sixt.jar

ENTRYPOINT ["java", "-jar", "sixt.jar"]

EXPOSE 8080