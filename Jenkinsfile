pipeline {
    agent {
        node { label 'aws && build && linux && ubuntu' }
    }
    parameters {
        booleanParam(name: 'SKIP_BUILD', defaultValue: false, description: 'Skips Docker builds')
        string(name: 'AWS_REGION', defaultValue: 'us-east-1', description: 'AWS Region to deploy')
        string(name: 'KUBERNETES_CLUSTER_NAME', defaultValue: 'kube-eks-ci-compute', description: 'Kubernetes cluster to deploy')
    }
    environment {
        PROJECT_NAME = "WIPP-backend"
        ARTIFACT_PATH = "wipp-backend-application/target"
        ARTIFACT_ID = """${sh (
            script: 'xmllint --xpath "/*[local-name()=\'project\']/*[local-name()=\'artifactId\']/text()" wipp-backend-application/pom.xml',
            returnStdout: true
        )}"""
        ARTIFACT_VERSION = """${sh (
            script: 'xmllint --xpath "/*[local-name()=\'project\']/*[local-name()=\'version\']/text()" wipp-backend-application/pom.xml',
            returnStdout: true
        )}"""
        ARTIFACT_CLASSIFIER = """${sh (
            script: 'xmllint --xpath "//*[local-name()=\'classifier\']/text()" wipp-backend-application/pom.xml',
            returnStdout: true
        )}"""
        DOCKER_VERSION = readFile(file: 'deploy/docker/VERSION')
        WIPP_PVC_NAME = "wipp-pv-claim"
        STORAGE_CLASS_NAME = "rook-cephfs"
        STORAGE_WIPP = "50Gi"
        STORAGE_MONGO = "5Gi"
    }
    triggers {
        pollSCM('H/2 * * * *')
    }
    stages {
        stage('Build Version') {
            steps{
                script {
                    BUILD_VERSION_GENERATED = VersionNumber(
                        versionNumberString: 'v${BUILD_YEAR, XX}.${BUILD_MONTH, XX}${BUILD_DAY, XX}.${BUILDS_TODAY}',
                        projectStartDate:    '1970-01-01',
                        skipFailedBuilds:    true)
                    currentBuild.displayName = BUILD_VERSION_GENERATED
                    env.BUILD_VERSION = BUILD_VERSION_GENERATED
                }
            }
        }
        stage('Load config file') {
            steps {
                // Config JSON file is stored in Jenkins and should contain sensitive environment values.
                configFileProvider([configFile(fileId: 'env-ci', targetLocation: 'env-ci.json')]) {
                    script {
                        def urls = readJSON file: 'env-ci.json'

                        env.ARTIFACTORY_URL = urls.ARTIFACTORY_URL
                        env.ELASTIC_APM_URL = urls.ELASTIC_APM_URL
                        env.BACKEND_HOST_NAME = urls.BACKEND_HOST_NAME
                        env.MONGO_HOST_NAME = urls.MONGO_HOST_NAME
                    }
                }
            }
        }
        stage('Checkout source code') {
            steps {
                cleanWs()
                checkout scm
            }
        }
        stage('Build Maven Artifact') {
            when {
                environment name: 'SKIP_BUILD', value: 'false'
            }
            steps {
                withCredentials([string(credentialsId: 'ARTIFACTORY_USER', variable: 'ARTIFACTORY_USER'),
                                string(credentialsId: 'ARTIFACTORY_TOKEN', variable: 'ARTIFACTORY_TOKEN')]) {
                    script {
                        sh 'mvn clean package -P prod'
                        sh 'touch ${BUILD_VERSION}.tar.gz && tar --exclude=${BUILD_VERSION}.tar.gz -czf ${BUILD_VERSION}.tar.gz .'
                        env.ARTIFACT_NAME = env.ARTIFACT_ID + "-" + env.ARTIFACT_VERSION + "-" + env.ARTIFACT_CLASSIFIER + ".war"

                        sh "curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${BUILD_VERSION}.tar.gz ${ARTIFACTORY_URL}/${PROJECT_NAME}/${BUILD_VERSION}.tar.gz"
                        sh "curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${ARTIFACT_PATH}/${ARTIFACT_NAME} ${ARTIFACTORY_URL}/${PROJECT_NAME}/${ARTIFACT_NAME}"
                    }                       
                }
            }
        }
        stage('Build Docker') {
            when {
                environment name: 'SKIP_BUILD', value: 'false'
            }
            steps {
                withCredentials([string(credentialsId: 'ARTIFACTORY_USER', variable: 'ARTIFACTORY_USER'),
                                    string(credentialsId: 'ARTIFACTORY_TOKEN', variable: 'ARTIFACTORY_TOKEN')]) {
                    script {
                        docker.withRegistry('https://registry-1.docker.io/v2/', 'f16c74f9-0a60-4882-b6fd-bec3b0136b84') {
                            def image = docker.build("labshare/wipp-backend:latest", "--build-arg SOURCE_FOLDER=. --build-arg ARTIFACTORY_USER=${ARTIFACTORY_USER} --build-arg ARTIFACTORY_TOKEN=${ARTIFACTORY_TOKEN} --no-cache ./")
                            image.push()
                            image.push(env.DOCKER_VERSION)
                        }
                    }
                }
            }
        }
        stage('Deploy WIPP to Kubernetes') {
            steps {
                dir('deploy/kubernetes') {
                    script {                        
                        sh "sed -i 's/STORAGE_WIPP_VALUE/${STORAGE_WIPP}/g' storage-ceph.yaml"
                        sh "sed -i 's/WIPP_PVC_NAME_VALUE/${WIPP_PVC_NAME}/g' storage-ceph.yaml"
                        sh "sed -i 's/STORAGE_CLASS_NAME_VALUE/${STORAGE_CLASS_NAME}/g' storage-ceph.yaml"
                        sh "sed -i 's/STORAGE_MONGO_VALUE/${STORAGE_MONGO}/g' storage-ceph.yaml"
                        sh "sed -i 's/BACKEND_VERSION_VALUE/${DOCKER_VERSION}/g' backend-deployment.yaml"
                        sh "sed -i 's/WIPP_PVC_NAME_VALUE/${WIPP_PVC_NAME}/g' backend-deployment.yaml"
                        sh "sed -i 's|ELASTIC_APM_URL_VALUE|${ELASTIC_APM_URL}|g' backend-deployment.yaml"
                        sh "sed -i 's/BACKEND_HOST_NAME_VALUE/${BACKEND_HOST_NAME}/g' services.yaml"
                        sh "sed -i 's/MONGO_HOST_NAME_VALUE/${MONGO_HOST_NAME}/g' services.yaml"
                    }
                    withAWS(credentials:'aws-jenkins-eks') {
                        sh "aws --region ${AWS_REGION} eks update-kubeconfig --name ${KUBERNETES_CLUSTER_NAME}"
                        sh '''
                            kubectl apply -f storage-ceph.yaml
                            kubectl apply -f mongo-deployment.yaml
                            kubectl apply -f backend-deployment.yaml
                            kubectl apply -f services.yaml
                        '''
                    }
                }
            }
        }
    }
}