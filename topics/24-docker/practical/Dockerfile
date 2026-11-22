# syntax=docker/dockerfile:1
# Multi-stage image for any Spring Boot practical in this repository.
# Build from the REPOSITORY ROOT (the build context must contain mvnw and topics/):
#   docker build -f topics/24-docker/practical/Dockerfile \
#     --build-arg MODULE=23-integration-testing/practical/integration-tests-demo -t jdk/inventory .

ARG MODULE=20-spring-boot/practical/spring-boot-library

# ---------- 1. build: full JDK + Maven wrapper (thrown away afterwards) ----------
FROM eclipse-temurin:21-jdk AS build
ARG MODULE
WORKDIR /src
COPY mvnw ./
COPY .mvn .mvn
COPY topics topics
# BuildKit cache mount: Maven's ~/.m2 survives between builds without ending up in the image
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -q -f topics/pom.xml -pl "$MODULE" -am package -DskipTests \
    && cp topics/$MODULE/target/*.jar /src/application.jar

# ---------- 2. extract: split the fat jar into layers (rarely-changing first) ----------
FROM eclipse-temurin:21-jre-alpine AS extract
WORKDIR /builder
COPY --from=build /src/application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------- 3. runtime: small JRE image, non-root, layer-friendly ----------
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /application
# each COPY is a separate image layer: changing your code only rebuilds/pushes the last (small) one
COPY --from=extract /builder/extracted/dependencies/ ./
COPY --from=extract /builder/extracted/spring-boot-loader/ ./
COPY --from=extract /builder/extracted/snapshot-dependencies/ ./
COPY --from=extract /builder/extracted/application/ ./
USER app
EXPOSE 8080
# size the heap from the container memory limit, not the host's
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "application.jar"]
