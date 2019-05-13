/*
  This Jenkinsfile is designed to provide PR building functionality for Decima
  Property of Socrata / Tyler Technologies
*/
pipeline {
    options {
      timeout(time: 60, unit: 'MINUTES')
      ansiColor('xterm')
    }
    triggers {
        issueCommentTrigger('.*test the pipeline.*')
    }

    parameters {
        string(name: 'AGENT', defaultValue: 'worker-pg9x-bionic-chef14', description: 'Which build agent to use?')
    }
    agent { label params.AGENT }
    environment {
        SERVICE = 'decima'
        WORKSPACE = pwd()
    }
    stages {
        stage('Initialize Job Details') {
            steps {
                script {
                    currentBuild.description = "${env.SERVICE} : ${env.GIT_COMMIT} : ${env.NODE_NAME}"
                }
            }
        }
        stage('Show Job Config') {
            steps {
                echo '-------------------------------'
                echo "SERVICE:              ${SERVICE}"
                echo "WORKSPACE:            ${WORKSPACE}"
                echo "HOME:                 ${HOME}"
                echo '-------------------------------'
            }
        }
        stage("Compile Decima PR") {
            steps {
                sh "sbt compile"
            }
        }
        stage("Assemble Decima PR") {
            steps {
                sh "sbt assembly"
            }
        }
        stage('Package Decima PR') {
            steps {
                sh "sbt package"
            }
        }
        stage("Test Decima PR") {
            steps {
                githubNotify context: "Test Decima PR", description: 'Building and testing the code...', status: 'PENDING'
                sh "sbt test"
            }
            post {
                success {
                    githubNotify context: "Test Decima PR", description: 'No build or test errors were found.', status: 'SUCCESS'
                }

                failure {
                    githubNotify context: "Test Decima PR", description: 'Build or test errors were found.', status: 'FAILURE'
                }
            }
        }
    }
}
