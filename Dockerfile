FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app

COPY --from=builder --chown=app:app /app/target/*.jar app.jar

USER app
EXPOSE 4110

ENTRYPOINT ["java", "-jar", "app.jar"]
