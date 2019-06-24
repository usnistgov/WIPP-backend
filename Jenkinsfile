pipeline {

    agent {
        node { label 'aws && build && linux && ubuntu'}
    }
	parameters {
        string(name: 'BUILD_VERSION', defaultValue: '', description: '')
    }
	triggers {
        pollSCM('H/2 * * * *')
    }
     stages {
	stage('Build Version'){
            when { expression { return !params.BUILD_VERSION } }
            steps{
                script {
                    BUILD_VERSION_GENERATED = VersionNumber(
                        versionNumberString: 'v${BUILD_YEAR, XX}.${BUILD_MONTH, XX}${BUILD_DAY, XX}.${BUILDS_TODAY}',
                        projectStartDate:    '1970-01-01',
                        skipFailedBuilds:    true)
                    currentBuild.displayName = BUILD_VERSION_GENERATED
                    env.BUILD_VERSION = BUILD_VERSION_GENERATED
                    env.BUILD = 'true'
                }
            }
        }
	     stage('Checkout source code') {
            steps {
                cleanWs()
                checkout scm
            }
        }
	    stage('Build Maven & Pushing Artifactory') {
		      steps {
            script {   
			   sh 'mvn clean install'
			   sh 'tar cvf ${BUILD_VERSION}.tar.gz .'
		   }
	    withCredentials([string(credentialsId: 'ARTIFACTORY_USER', variable: 'ARTIFACTORY_USER'),
                            string(credentialsId: 'ARTIFACTORY_TOKEN', variable: 'ARTIFACTORY_TOKEN')]) {
                   sh "curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T ${BUILD_VERSION}.tar.gz https://builds.aws.labshare.org/artifactory/labshare/WIPP-backend/${BUILD_VERSION}.tar.gz"
                   sh "curl -u${ARTIFACTORY_USER}:${ARTIFACTORY_TOKEN} -T wipp-backend-application/target/wipp-backend-application-3.0.0-SNAPSHOT.war https://builds.aws.labshare.org/artifactory/labshare/WIPP-backend/wipp-backend.war"   
	                 }
	             }  
	       }
        stage('Docker build & push image to AWS ECR') {
            steps {
		    withCredentials([string(credentialsId: 'ARTIFACTORY_USER', variable: 'ARTIFACTORY_USER'),
                               string(credentialsId: 'ARTIFACTORY_TOKEN', variable: 'ARTIFACTORY_TOKEN')]) {
                script {
                docker.build("wipp_backend", "--build-arg SOURCE_FOLDER=. --build-arg ARTIFACTORY_USER=${ARTIFACTORY_USER} --build-arg ARTIFACTORY_TOKEN=${ARTIFACTORY_TOKEN} --no-cache ./")  
		
		docker.withRegistry('https://684150170045.dkr.ecr.us-east-1.amazonaws.com', 'ecr:us-east-1:aws-jenkins-build') {
                      docker.image("wipp_backend").push("current")
                                    }
	                            }
                        }
                  }      
            }
       }
}
