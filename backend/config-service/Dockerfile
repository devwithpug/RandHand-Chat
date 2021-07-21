FROM gradle:7.0.0-jdk11-openj9 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY apiEncryptionKey.jks apiEncryptionKey.jks
COPY --from=build /home/gradle/src/build/libs/*.jar ConfigService.jar
ENTRYPOINT [ "java", "-jar", "ConfigService.jar" ]