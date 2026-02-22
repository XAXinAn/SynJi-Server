ARG BASE_REGISTRY=docker.io/library

FROM ${BASE_REGISTRY}/maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY docker/maven/settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src

RUN mvn -B -s /root/.m2/settings.xml -DskipTests clean package

FROM ${BASE_REGISTRY}/eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd --create-home --uid 10001 spring
COPY --from=builder /workspace/target/*.jar /app/app.jar

USER spring
EXPOSE 8080

ENV TZ=Asia/Shanghai
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
