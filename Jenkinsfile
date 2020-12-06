pipeline {
    agent {
        label 'buster'
    }

    stages {
        stage('Build') {
            steps {
                sh 'lein uberjar'
                archiveArtifacts artifacts: 'target/*-standalone.jar'
            }
        }
    }
}
