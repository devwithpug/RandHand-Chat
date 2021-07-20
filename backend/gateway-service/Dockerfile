FROM openjdk:17-ea-11-slim
VOLUME /tmp
COPY build/libs/gateway-service-0.1.jar GatewayService.jar
ENTRYPOINT [ "java", "-jar", "GatewayService.jar" ]