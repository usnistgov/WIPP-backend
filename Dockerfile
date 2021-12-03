
# Stage 0: Build image
FROM maven:3.8.3-jdk-8

ARG BACKEND_ROOT="WIPP-backend"
ARG BACKEND_APP="wipp-backend-application"
ARG BACKEND_DATA="wipp-backend-data"
ARG BACKEND_CORE="wipp-backend-core"
ARG BACKEND_ARGO="wipp-backend-argo-workflows"

COPY pom.xml /usr/local/${BACKEND_ROOT}/pom.xml
COPY ${BACKEND_NAME} /usr/local/${BACKEND_ROOT}/${BACKEND_NAME}
COPY ${BACKEND_DATA} /usr/local/${BACKEND_ROOT}/${BACKEND_DATA}
COPY ${BACKEND_CORE} /usr/local/${BACKEND_ROOT}/${BACKEND_CORE}
COPY ${BACKEND_ARGO} /usr/local/${BACKEND_ROOT}/${BACKEND_ARGO}
COPY deploy/docker/maven-settings.xml /usr/share/maven/conf/settings.xml
WORKDIR /usr/local/${BACKEND_ROOT}
RUN mvn clean package -P prod

# Stage 1: Runtime image
FROM openjdk:8-jdk-alpine
LABEL org.opencontainers.image.authors="National Institute of Standards and Technology"

EXPOSE 8080

ARG BACKEND_ROOT="WIPP-backend"
ARG BACKEND_APP="wipp-backend-application"
ARG EXEC_DIR="/opt/wipp"
ARG DATA_DIR="/data/WIPP-plugins"
ARG ARGO_VERSION="v2.3.0"
ARG APM_VERSION="1.9.0"

COPY deploy/docker/VERSION /VERSION

# Create exec and data folders
RUN mkdir -p \
  ${EXEC_DIR}/config \
  ${DATA_DIR}

# Download Elastic APM Java Agent
RUN wget https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/${APM_VERSION}/elastic-apm-agent-${APM_VERSION}.jar -O ${EXEC_DIR}/elastic-apm-agent.jar

# Install Argo CLI executable
RUN wget https://github.com/argoproj/argo-workflows/releases/download/${ARGO_VERSION}/argo-linux-amd64 && \
    chmod +x argo-linux-amd64 && \
    mv argo-linux-amd64 /usr/local/bin/argo

# Copy WIPP backend application exec WAR from the previous stage
COPY --from=0 /usr/local/${BACKEND_ROOT}/${BACKEND_APP}/target/${BACKEND_APP}-*-exec.war ${EXEC_DIR}/wipp-backend.war

# Copy properties and entrypoint script
COPY deploy/docker/application.properties ${EXEC_DIR}/config
COPY deploy/docker/entrypoint.sh /usr/local/bin

# Set working directory
WORKDIR ${EXEC_DIR}

# Entrypoint
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
