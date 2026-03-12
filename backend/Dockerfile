# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx768m", \
  "-XX:+UseG1GC", \
  "-XX:G1HeapRegionSize=4m", \
  "-XX:+UseStringDeduplication", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]