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
                configFileProvider([configFile(fileId: '2e7ca7c3-751d-46bf-a8ed-94faa706ba22', targetLocation: 'artifactory_url')]) {
                    script {
                        env.ARTIFACTORY_URL = readFile(file: 'artifactory_url')
                    }
                }
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
                configFileProvider([configFile(fileId: 'env-ci', targetLocation: '.env')]) {
                    withAWS(credentials:'aws-jenkins-eks') {
                        sh "aws --region ${AWS_REGION} eks update-kubeconfig --name ${KUBERNETES_CLUSTER_NAME}"
                        sh "./deploy.sh"
                    }
                }
            }
        }
    }
}