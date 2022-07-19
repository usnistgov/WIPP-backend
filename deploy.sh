 #!/usr/bin/env bash

export $(egrep -v '^#' .env)

# Backup file extension required to support Mac versions of sed
sed -i.bak \
    -e "s/STORAGE_WIPP_VALUE/${STORAGE_WIPP}/g" \
    -e "s/WIPP_PVC_NAME_VALUE/${WIPP_PVC_NAME}/g" \
    -e "s/STORAGE_CLASS_NAME_VALUE/${STORAGE_CLASS_NAME}/g" \
    -e "s/STORAGE_MONGO_VALUE/${STORAGE_MONGO}/g" \
    deploy/kubernetes/storage.yaml
rm deploy/kubernetes/storage.yaml.bak

sed -i.bak \
    -e "s/BACKEND_VERSION_VALUE/${DOCKER_VERSION}/g" \
    -e "s/WIPP_PVC_NAME_VALUE/${WIPP_PVC_NAME}/g" \
    -e "s|ELASTIC_APM_URL_VALUE|${ELASTIC_APM_URL}|g" \
    -e "s|NODE_SELECTOR_VALUE|${NODE_SELECTOR}|g" \
    -e "s|TOLERATIONS_VALUE|${TOLERATIONS}|g" \
    -e "s|OME_CONVERTER_THREADS_VALUE|${OME_CONVERTER_THREADS}|g" \
    -e "s|KEYCLOAK_AUTH_URL_VALUE|${KEYCLOAK_AUTH_URL}|g" \
    -e "s|WORKFLOW_PLUGINHARDWAREREQUIREMENTS_ENABLED_VALUE|${WORKFLOW_PLUGINHARDWAREREQUIREMENTS_ENABLED}|g" \
    deploy/kubernetes/backend-deployment.yaml
rm deploy/kubernetes/backend-deployment.yaml.bak

sed -i.bak \
    -e "s|BACKEND_HOST_NAME_VALUE|${BACKEND_HOST_NAME}|g" \
    -e "s|TENSORBOARD_HOST_NAME_VALUE|${TENSORBOARD_HOST_NAME}|g" \
    deploy/kubernetes/ingress.yaml
rm deploy/kubernetes/ingress.yaml.bak

sed -i.bak \
    -e "s/WIPP_PVC_NAME_VALUE/${WIPP_PVC_NAME}/g" \
    deploy/kubernetes/tensorboard-deployment.yaml
rm deploy/kubernetes/tensorboard-deployment.yaml.bak

kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/storage.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/mongo-deployment.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/services.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/backend-deployment.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/tensorboard-deployment.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/ingress.yaml
