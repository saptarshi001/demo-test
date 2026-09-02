import groovy.json.JsonOutput

pipeline {
    agent any

    environment {
        LOG_ANALYZER_URL = 'http://host.docker.internal:8082'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    try {
                        echo 'Building application...'
                        sh 'mvn clean package -DskipTests'
                    } catch (Exception e) {
                        env.FAILED_STAGE = 'Build'
                        throw e
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    try {
                        echo 'Running tests...'
                        sh 'mvn test'
                    } catch (Exception e) {
                        env.FAILED_STAGE = 'Test'
                        throw e
                    }
                }
            }
        }
    }

    post {

        failure {
            script {

                echo '========================================='
                echo 'BUILD FAILED - STARTING AI LOG ANALYSIS'
                echo '========================================='

                def buildId = "${env.JOB_NAME}-${env.BUILD_NUMBER}"

                def failedStage = env.FAILED_STAGE ?: 'Unknown'

                echo "Build ID: ${buildId}"
                echo "Job Name: ${env.JOB_NAME}"
                echo "Failed Stage: ${failedStage}"

                // Capture last 100 lines of Jenkins console
                def consoleLogs = currentBuild.rawBuild
                        .getLog(100)
                        .join('\n')

                def query = """
JOB NAME: ${env.JOB_NAME}
BUILD ID: ${buildId}
STATUS: FAILED
FAILED STAGE: ${failedStage}

ACTUAL JENKINS ERROR LOGS:
${consoleLogs}
"""

                // Safely generate JSON
                def payload = JsonOutput.toJson([
                    query: query
                ])

                writeFile(
                    file: 'failure-analysis.json',
                    text: payload
                )

                echo 'Sending failure to DevOps Log Analyzer...'

                sh '''
                    curl -X POST "$LOG_ANALYZER_URL/logs/analyze" \
                      -H "Content-Type: application/json" \
                      --data @failure-analysis.json
                '''
            }
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}
