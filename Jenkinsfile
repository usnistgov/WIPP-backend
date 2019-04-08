pipeline {

    agent {
        node { label 'aws && build && linux && ubuntu'}
    }

    stages {
            
        stage('Docker build') {
            steps {
                script {
	    docker.withRegistry("${ECRADDRESS}", 'ecr:us-east-1:aws-jenkins-build') {
                        docker.build("wipp_backend", "--build-arg SOURCE_FOLDER=. --no-cache ./")
	    docker.image("wipp_backend").push("${BUILD_ID}")
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
