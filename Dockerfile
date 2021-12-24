FROM openjdk:16-slim-buster

COPY build/libs/MsMinio-0.0.1-SNAPSHOT.jar .

ENTRYPOINT java -jar MsMinio-0.0.1-SNAPSHOT.jar