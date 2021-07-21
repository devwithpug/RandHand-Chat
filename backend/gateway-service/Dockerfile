FROM gradle:7.0.0-jdk11-openj9 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:17-ea-11-slim
VOLUME /tmp
COPY --from=build /home/gradle/src/build/libs/*.jar GatewayService.jar
ENTRYPOINT [ "java", "-jar", "GatewayService.jar" ]