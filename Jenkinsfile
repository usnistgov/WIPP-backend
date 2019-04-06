pipeline {

    agent {
        node { label 'aws && build && linux && ubuntu'}
    }

    stages {
        
        stage('Build App') {
            steps {
                
	    sh 'mvn clean package'
            }
        }
        stage('Docker build') {
            steps {
                script {
	    docker.withRegistry('https://684150170045.dkr.ecr.us-east-1.amazonaws.com', 'ecr:us-east-1:aws-jenkins-build') {
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
