FROM maven:3.6.1-jdk-8

EXPOSE 8087

ENV DEFAULT_JAR_NAME integrations.jar

ENV BASE_DIR /opt/distil_integrations
ENV SETTINGS_FILE /usr/share/maven/conf/settings.xml
ENV DEFAULT_PROFILE default

WORKDIR ${BASE_DIR}
ADD pom.xml ${BASE_DIR}
ADD .mvn-settings.xml ${SETTINGS_FILE}
RUN mvn -B -f ./pom.xml -s ${SETTINGS_FILE} dependency:resolve

COPY ./ ${BASE_DIR}
#tests will run outside of the docker
RUN mvn clean install -s ${SETTINGS_FILE}  -Dmaven.test.skip=true

ENTRYPOINT java -Dspring.profiles.active=${DEFAULT_PROFILE} -cp target/${DEFAULT_JAR_NAME} ai.distil.integration.IntegrationApp
