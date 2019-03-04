
pipeline {

    agent {
        node { label 'internal-build.ncats'}
    }

    stages {
        stage('SCM checkout'){
            steps{
                script {
                    sh 'git clone https://github.com/gayathrikurapati/WIPP-backend.git'
                }
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
