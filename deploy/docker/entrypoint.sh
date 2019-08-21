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

java -jar /opt/wipp/wipp-backend.war &

if [[ -n ${ELASTIC_APM_SERVER_URLS} && -n ${ELASTIC_APM_SERVICE_NAME} ]]; then
  # Checks if the Spring Boot application has started
  while ! { cat /opt/wipp/logs/spring.log | grep -q 'Started'; }; do
    sleep 1
  done

  WIPP_PID=`pgrep -f wipp-backend`

  # Attaches the Elastic APM Agent
  java -jar apm-agent-attach.jar --pid ${WIPP_PID}
fi

# Keeps the Docker container running until the background process (WIPP-backend) closes
wait
