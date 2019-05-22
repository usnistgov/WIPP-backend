pipeline {

    agent {
        node { label 'aws && build && linux && ubuntu'}
    }

    stages {
            
        stage('Docker build') {
            steps {
                script {
	    
                        docker.build("wipp_backend", "--build-arg SOURCE_FOLDER=. --no-cache ./")             
	          }
        }
     }      
   }
}
