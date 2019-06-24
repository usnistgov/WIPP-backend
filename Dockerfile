FROM openjdk:11-jdk

EXPOSE 8080

RUN mkdir -p /opt/wipp/config

WORKDIR "/opt/wipp"

ARG ARTIFACTORY_USER

ARG ARTIFACTORY_TOKEN

RUN curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -O https://builds.aws.labshare.org/artifactory/labshare/WIPP-backend/wipp-backend.war

COPY application.properties /opt/wipp/config/

COPY entrypoint.sh /usr/local/bin

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
