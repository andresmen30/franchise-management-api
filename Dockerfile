# syntax=docker/dockerfile:1

FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B clean package -DskipTests
RUN java -Djarmode=tools -jar target/*.jar extract --layers --destination /build/layers

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S -G spring spring
WORKDIR /app
COPY --from=build --chown=spring:spring /build/layers/dependencies/lib ./lib
COPY --from=build --chown=spring:spring /build/layers/application/*.jar ./app.jar
USER spring
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -q -O - http://localhost:8080/actuator/health/liveness | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
