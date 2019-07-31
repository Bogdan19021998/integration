FROM maven:3.6.1-jdk-8

EXPOSE 80

ENV BASE_DIR /opt/distil_integrations
ENV DEFAULT_JAR_NAME integrations.jar
ENV JAR_PATH target/${DEFAULT_JAR_NAME}

ENV SETTINGS_FILE /usr/share/maven/conf/settings.xml

WORKDIR ${BASE_DIR}
ADD pom.xml ${BASE_DIR}
ADD .mvn-settings.xml ${SETTINGS_FILE}
RUN mvn dependency:go-offline -s ${SETTINGS_FILE}

COPY ./ ${BASE_DIR}
#tests will run outside of the docker
RUN mvn package -s ${SETTINGS_FILE} -Dmaven.test.skip=true

FROM openjdk:8u212-jre-slim

ENV DEFAULT_PROFILE staging

ENV BASE_DIR /opt/distil_integrations
ENV DEFAULT_JAR_NAME integrations.jar
ENV JAR_PATH target/${DEFAULT_JAR_NAME}

WORKDIR ${BASE_DIR}
COPY --from=0 ${BASE_DIR}/${JAR_PATH} ${BASE_DIR}
ENTRYPOINT exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dspring.profiles.active=${DEFAULT_PROFILE} -cp ${BASE_DIR}/${DEFAULT_JAR_NAME} ai.distil.integration.IntegrationApp
