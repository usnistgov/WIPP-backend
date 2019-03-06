
pipeline {

    agent {
        node { label 'aws && build && linux && ubuntu'}
    }

    stages {
        stage('Pre-requisites') {
            steps {
                sh 'echo "Installing pre-requisites "'
		//sh 'sudo apt-get update'
		//sh 'sudo apt-get install maven -y'
		//sh 'sudo apt-get install docker.io -y'
		//sh 'sudo apt-get install openjdk-8-jdk -y'
		//sh 'sudo update-java-alternatives --set java-1.8.0-openjdk-amd64'
		    
            }
        }
        stage('Build App') {
            steps {
                
		    sh 'mvn package'
            }
        }
        stage('Docker build') {
            steps {
                script {
			docker.withRegistry(“https://registry-1.docker.io/v2/“, “f16c74f9-0a60-4882-b6fd-bec3b0136b84”) {
                          // Build and push the images to the registry
                          def image = docker.build(“labshare/wipp-backend:${env.BUILD_VERSION}“, “--no-cache --build-arg SOURCE_FOLDER=./${env.BUILD_VERSION}.”)
                          image.push(“${env.BUILD_VERSION}“)
                    	}
		}
	}
}
        
   }
    post {
        always {
            slackSend channel:'#build-notifications',
            color: 'good',
            message: "${env.BUILD_URL} has result ${currentBuild.result}"
        }
    }
}
