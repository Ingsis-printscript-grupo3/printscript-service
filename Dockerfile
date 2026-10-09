# compila con el JDK y el token
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

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

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
