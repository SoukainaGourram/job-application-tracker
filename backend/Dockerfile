# ==============================================================================
# JobTrack Backend — Dockerfile
# Minimal, secure JRE 21 runtime container
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

# Non-root user for security
RUN addgroup -S jobtrack && adduser -S jobtrack -G jobtrack
USER jobtrack:jobtrack

WORKDIR /app

# Copy executable Spring Boot JAR built with Java 21
COPY target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
