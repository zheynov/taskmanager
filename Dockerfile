FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine AS extract
WORKDIR /app
ARG JAR_FILE=target/task-manager-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 -S app && adduser -u 1001 -S app -G app
WORKDIR /app
COPY --from=extract app/dependencies/ ./
COPY --from=extract app/spring-boot-loader/ ./
COPY --from=extract app/snapshot-dependencies/ ./
COPY --from=extract app/application/ ./
USER app:app
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
