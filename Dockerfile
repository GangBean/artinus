FROM amazoncorretto:17.0.12-al2023-headless

WORKDIR /app

COPY ./build/libs/*.jar app.jar

ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
