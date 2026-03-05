# Étape 1 : Build
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
# On ajoute -Dmaven.test.skip=true pour ignorer la compilation des tests
RUN mvn clean package -Dmaven.test.skip=true

# Étape 2 : Exécution
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/SGMbackend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
