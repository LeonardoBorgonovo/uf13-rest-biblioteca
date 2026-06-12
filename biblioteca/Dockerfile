# ST1: BUILD
FROM maven:4.0.0-rc-4-amazoncorretto-21-debian AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


# ST2: RUN
FROM amazoncorretto:21-alpine3.21-jre AS RUN
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /app/target/*.jar biblioteca.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "biblioteca.jar"]