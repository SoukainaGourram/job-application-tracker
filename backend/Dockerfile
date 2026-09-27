# ==============================================================================
# JobTrack Backend — Dockerfile
# Expects the JAR to be pre-built locally with: mvn package -DskipTests
# The JAR is then copied into a minimal JRE 21 runtime image.
#
# Why not multi-stage? Docker Desktop on this machine has no internet access,
# so Maven cannot download dependencies inside the build container.
# Build locally first: mvn package -DskipTests
# Then: docker compose build
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

# Non-root user for security
RUN addgroup -S jobtrack && adduser -S jobtrack -G jobtrack
USER jobtrack:jobtrack

WORKDIR /app

# Copy the pre-built Spring Boot executable JAR
COPY target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
