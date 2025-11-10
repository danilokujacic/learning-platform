FROM eclipse-temurin:24-jdk-alpine AS build

WORKDIR /app

COPY mvnw .

COPY .mvn .mvn

RUN chmod +x mvnw

COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw package -DskipTests

FROM eclipse-temurin:24-jdk-alpine AS project

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8888

ENV JAVA_OPTIONS="\
-server \
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-XX:InitialRAMPercentage=75.0 \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200"


ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTIONS -jar app.jar --spring.profiles.active=prod"]