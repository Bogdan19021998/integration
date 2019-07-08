FROM maven:3.6.1-jdk-8

EXPOSE 8087

ENV BASE_DIR /opt/distil_integrations
ENV DEFAULT_JAR_NAME integrations.jar
ENV JAR_PATH target/${DEFAULT_JAR_NAME}

ENV SETTINGS_FILE /usr/share/maven/conf/settings.xml

WORKDIR ${BASE_DIR}
ADD pom.xml ${BASE_DIR}
ADD .mvn-settings.xml ${SETTINGS_FILE}
RUN mvn -B -f ./pom.xml -s ${SETTINGS_FILE} dependency:resolve

COPY ./ ${BASE_DIR}
#tests will run outside of the docker
RUN mvn clean install -s ${SETTINGS_FILE}  -Dmaven.test.skip=true

FROM openjdk:8u212-jre-slim
ENV DEFAULT_PROFILE default

ENV BASE_DIR /opt/distil_integrations
ENV DEFAULT_JAR_NAME integrations.jar
ENV JAR_PATH target/${DEFAULT_JAR_NAME}

WORKDIR ${BASE_DIR}
COPY --from=0 ${BASE_DIR}/${JAR_PATH} ${BASE_DIR}
ENTRYPOINT java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dspring.profiles.active=${DEFAULT_PROFILE} -cp ${BASE_DIR}/${DEFAULT_JAR_NAME} ai.distil.integration.IntegrationApp
