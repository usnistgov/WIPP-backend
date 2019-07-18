FROM openjdk:8-jdk-alpine
MAINTAINER National Institute of Standards and Technology

EXPOSE 8080

ARG BACKEND_NAME="wipp-backend-application"
ARG EXEC_DIR="/opt/wipp"
ARG DATA_DIR="/data/WIPP-plugins"

# Create exec and data folders
RUN mkdir -p \
  ${EXEC_DIR}/config \
  ${DATA_DIR}

# Copy WIPP backend application exec WAR
COPY ${BACKEND_NAME}/target/${BACKEND_NAME}-*-exec.war ${EXEC_DIR}/wipp-backend.war

# Copy properties and entrypoint script
COPY docker/application.properties /opt/wipp/config
COPY docker/entrypoint.sh /usr/local/bin

# Set working directory
WORKDIR "/opt/wipp"

# Entrypoint
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
