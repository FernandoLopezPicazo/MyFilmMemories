# Build: compila el JAR con Maven + JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# Run: solo el JRE, sin herramientas de build, imagen final ligera
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/seriestracker-0.0.1-SNAPSHOT.jar app.jar

# Render inyecta PORT en tiempo de ejecución (ver application.properties:
# server.port=${PORT:8090})
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
