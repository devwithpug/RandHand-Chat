FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/eureka-server-0.1.jar EurekaServer.jar
ENTRYPOINT [ "java", "-jar", "EurekaServer.jar" ]