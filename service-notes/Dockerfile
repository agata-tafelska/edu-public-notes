FROM adoptopenjdk/openjdk11:alpine-jre
LABEL name=service-notes

ENV APP_NAME=service-notes \
    SERVER_PORT=8080

EXPOSE ${SERVER_PORT}

RUN apk update && apk upgrade

COPY build/libs/${APP_NAME}*.jar /${APP_NAME}.jar

ENTRYPOINT java -jar /${APP_NAME}.jar
