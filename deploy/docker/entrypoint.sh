#!/bin/sh
if [ $# -ne 3 ]
then
  echo "Illegal number of parameters. Exiting..."
  echo "Command: ./entrypoint.sh \${mongo_host} \${mongo_port} \${shared_pvc}"
  exit 1
fi

MONGO_HOST=$1
MONGO_PORT=$2
SHARED_PVC=$3

if [[ -z $KEYCLOAK_AUTH_URL ]]; then
  echo "Missing Keycloak auth server URL env var."
  exit 1
fi

if [[ -z $KEYCLOAK_SSL_REQUIRED ]]; then
  KEYCLOAK_SSL_REQUIRED="external"
fi

if [[ -z $KEYCLOAK_DISABLE_TRUST_MANAGER ]]; then
  KEYCLOAK_DISABLE_TRUST_MANAGER="false"
fi

if [[ -z $OME_CONVERTER_THREADS ]]; then
  OME_CONVERTER_THREADS="6"
fi

sed -i \
  -e 's/@mongo_host@/'"${MONGO_HOST}"'/' \
  -e 's/@mongo_port@/'"${MONGO_PORT}"'/' \
  -e 's/@shared_pvc@/'"${SHARED_PVC}"'/' \
  -e 's|@keycloak_auth_url@|'"${KEYCLOAK_AUTH_URL}"'|' \
  -e 's|@keycloak_ssl_required@|'"${KEYCLOAK_SSL_REQUIRED}"'|' \
  -e 's|@keycloak_disable_trust_manager@|'"${KEYCLOAK_DISABLE_TRUST_MANAGER}"'|' \
  -e 's|@workflow_nodeSelector@|'"${NODE_SELECTOR}"'|' \
  -e 's|@workflow_tolerations@|'"${TOLERATIONS}"'|' \
  -e 's|@ome_converter_threads@|'"${OME_CONVERTER_THREADS}"'|' \
  /opt/wipp/config/application.properties

if [[ -n ${ELASTIC_APM_SERVER_URLS} && -n ${ELASTIC_APM_SERVICE_NAME} ]]; then
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -javaagent:/opt/wipp/elastic-apm-agent.jar"
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Delastic.apm.service_name=$ELASTIC_APM_SERVICE_NAME"
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Delastic.apm.application_packages=$ELASTIC_APM_APPLICATION_PACKAGES"
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Delastic.apm.server_urls=$ELASTIC_APM_SERVER_URLS"
fi

java -jar /opt/wipp/wipp-backend.war