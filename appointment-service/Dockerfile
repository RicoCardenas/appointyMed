# Contexto de construccion: la raiz del repositorio (docker build -f appointment-service/Dockerfile .)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY catalog-service/pom.xml catalog-service/
COPY appointment-service/pom.xml appointment-service/
COPY clinical-history-service/pom.xml clinical-history-service/
COPY notification-service/pom.xml notification-service/
COPY lombok.config .
COPY appointment-service/src appointment-service/src
RUN mvn -q -B -pl appointment-service -am -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 appointy
WORKDIR /app
COPY --from=build /src/appointment-service/target/appointment-service-*-SNAPSHOT.jar app.jar
USER appointy
# Heap acotado: el plan gratuito de Render ofrece 512 MB por servicio
ENV JAVA_TOOL_OPTIONS="-Xmx300m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
