FROM maven:4.0.0-rc-5-eclipse-temurin-21-alpine

ARG TEST_PROFILE=api
ARG APIBASEURI=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURI=${APIBASEURI}
ENV UIBASEURL=${UIBASEURL}

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY . .

USER root

CMD /bin/bash -c "\
    mkdir -p /app/logs;\
    echo '>>> Running tests with profile: ${TEST_PROFILE}';\
    mvn test -q -P ${TEST_PROFILE};\
    echo '>>> Running Maven Surefire report';\
    mvn -DskipTests=true surefire-report:report;\
    "