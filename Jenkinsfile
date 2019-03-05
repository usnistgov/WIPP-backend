
pipeline {

    agent {
        node { label 'aws && build && linux && ubuntu'}
    }

    stages {
        stage('Pre-requisites') {
            steps {
                sh 'sudo apt-get update'
		sh 'sudo apt-get install maven -y'
		sh 'sudo apt-get install docker.io'
		sh 'sudo apt-get install openjdk-8-jdk -y'
		sh 'sudo update-java-alternatives --set java-1.8.0-openjdk-amd64'
		    
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
			docker.withRegistry('https://684150170045.dkr.ecr.us-east-1.amazonaws.com', 'ecr:us-east-1:aws-jenkins-build') {
                        	docker.build("WIPP", "--build-arg SOURCE_FOLDER=. --no-cache ./")
				docker.image("${PROJECT_NAME}").push("${BUILD_VERSION}")
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
