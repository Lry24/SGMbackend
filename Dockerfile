# Étape 1 : Build
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Exécution (Correction ici)
FROM openjdk:17-jdk-slim
COPY --from=build /target/SGMbackend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
