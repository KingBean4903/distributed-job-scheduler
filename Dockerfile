FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --uid 1001 appuser

COPY target/*.jar app.jar

USER appuser

EXPOSE 9800

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

