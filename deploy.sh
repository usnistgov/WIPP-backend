#!/usr/bin/env bash

COLLECTION_PATH="/data/wipp/csv-collections/<id>"
ZOOM_CONFIG_FILE=sample-zoom-config.json

export $(egrep -v '^#' .env)

# Backup file extension required to support Mac versions of sed
sed -i.bak \
    -e "s/STORAGE_WIPP_VALUE/${STORAGE_WIPP}/g" \
    -e "s/WIPP_PVC_NAME_VALUE/${WIPP_PVC_NAME}/g" \
    -e "s/STORAGE_CLASS_NAME_VALUE/${STORAGE_CLASS_NAME}/g" \
    -e "s/STORAGE_MONGO_VALUE/${STORAGE_MONGO}/g" \
    deploy/kubernetes/storage-ceph.yaml
rm deploy/kubernetes/storage-ceph.yaml.bak

sed -i.bak \
    -e "s/BACKEND_VERSION_VALUE/${DOCKER_VERSION}/g" \
    -e "s/WIPP_PVC_NAME_VALUE/${WIPP_PVC_NAME}/g" \
    -e "s|ELASTIC_APM_URL_VALUE|${ELASTIC_APM_URL}|g" \
    deploy/kubernetes/backend-deployment.yaml
rm deploy/kubernetes/backend-deployment.yaml.bak

sed -i.bak \
    -e "s|BACKEND_HOST_NAME_VALUE|${BACKEND_HOST_NAME}|g" \
    -e "s|MONGO_HOST_NAME_VALUE|${MONGO_HOST_NAME}|g" \
    deploy/kubernetes/services.yaml
rm deploy/kubernetes/services.yaml.bak

kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/storage-ceph.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/mongo-deployment.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/services.yaml
kubectl apply --kubeconfig=${KUBECONFIG} -f deploy/kubernetes/backend-deployment.yaml