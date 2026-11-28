# compila con el JDK y el token
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# el agente de New Relic. Version fija: si no, cada build trae una distinta
ARG NEW_RELIC_AGENT_VERSION=9.5.0
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NEW_RELIC_AGENT_VERSION}/newrelic-java.zip /tmp/newrelic.zip
# jar xf: ya viene con el JDK, evita instalar unzip
RUN cd /tmp && jar xf newrelic.zip

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=$GITHUB_ACTOR
ENV GITHUB_TOKEN=$GITHUB_TOKEN

# las dependencias primero y el codigo despues
COPY gradle gradle
COPY gradlew settings.gradle.kts build.gradle.kts ./
# gradlew llega sin permiso de ejecucion desde Windows
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

COPY src src
RUN ./gradlew bootJar --no-daemon --quiet

# la imagen final arranca solo con el JRE y el jar
FROM eclipse-temurin:21-jre
WORKDIR /app

# el servicio ejecuta codigo que suben usuarios, mejor q no sea root
RUN useradd --system --uid 1001 printscript
USER printscript

COPY --from=build /app/build/libs/*.jar app.jar

# solo el jar y su config: el zip trae ademas javadocs y sources que no van a produccion
COPY --from=build /tmp/newrelic/newrelic.jar /tmp/newrelic/newrelic.yml /app/newrelic/
# el agente loguea a stdout y no a un archivo: el usuario no root no puede escribir en /app
ENV NEW_RELIC_LOG_FILE_NAME=STDOUT

EXPOSE 8080

# Docker (y despues Swarm) sabe si el servicio esta sano, no solo si el proceso corre: el update_config
# del stack espera a que el contenedor nuevo este healthy antes de bajar el viejo. curl ya viene en la
# imagen base. El start-period le da tiempo a Spring a arrancar
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# sin esto la JVM usa solo el 25% de la memoria que le da el contenedor
# -javaagent: el agente entra a la JVM antes que Spring. Su config son las variables NEW_RELIC_*
ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
