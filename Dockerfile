FROM openjdk:11-jdk

EXPOSE 8080

WORKDIR /usr/src/myapp

ARG BUILD_VERSION

ARG ARTIFACTORY_USER

ARG ARTIFACTORY_TOKEN

RUN curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -O https://builds.aws.labshare.org/artifactory/labshare/WIPP-backend/${BUILD_VERSION}.tar.gz

RUN tar -xvf ${BUILD_VERSION}.tar.gz -C /usr/src/myapp/

RUN rm -rf ${BUILD_VERSION}.tar.gz

ENTRYPOINT ["mvn", "spring-boot:run"]
