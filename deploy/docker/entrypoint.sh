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

sed -i \
  -e 's/@mongo_host@/'"${MONGO_HOST}"'/' \
  -e 's/@mongo_port@/'"${MONGO_PORT}"'/' \
  -e 's/@shared_pvc@/'"${SHARED_PVC}"'/' \
  /opt/wipp/config/application.properties

if [[ -n ${ELASTIC_APM_SERVER_URLS} && -n ${ELASTIC_APM_SERVICE_NAME} ]]; then
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -javaagent:/opt/wipp/elastic-apm-agent.jar"
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Delastic.apm.service_name=$ELASTIC_APM_SERVICE_NAME"
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Delastic.apm.application_packages=$ELASTIC_APM_APPLICATION_PACKAGES"
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Delastic.apm.server_urls=$ELASTIC_APM_SERVER_URLS"
fi

java -jar /opt/wipp/wipp-backend.war