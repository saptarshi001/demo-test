pipeline {
    agent any

    environment {
        LOG_ANALYZER_URL = 'http://host.docker.internal:8080'
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
                echo 'Building application...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }
    }

    post {

        failure {
            script {

                echo '========================================='
                echo 'BUILD FAILED - STARTING AI LOG ANALYSIS'
                echo '========================================='

                def failedStage = env.STAGE_NAME ?: 'Unknown'
                def buildId = "${env.JOB_NAME}-${env.BUILD_NUMBER}"

                echo "Build ID: ${buildId}"
                echo "Job Name: ${env.JOB_NAME}"
                echo "Failed Stage: ${failedStage}"

                /*
                 * Get Jenkins console output.
                 *
                 * currentBuild.rawBuild.getLog(500)
                 * retrieves the last 500 lines.
                 */
                def consoleLogs = currentBuild.rawBuild
                        .getLog(500)
                        .join('\n')

                /*
                 * Escape logs so they can safely be sent as JSON.
                 */
                def escapedLogs = consoleLogs
                        .replace('\\', '\\\\')
                        .replace('"', '\\"')
                        .replace('\n', '\\n')
                        .replace('\r', '')

                echo 'Sending failed build logs to DevOps Log Analyzer...'

                def payload = """
                {
                    "buildId": "${buildId}",
                    "jobName": "${env.JOB_NAME}",
                    "status": "FAILED",
                    "stageName": "${failedStage}",
                    "errorDetails": "${escapedLogs}",
                    "rootCause": "",
                    "resolution": ""
                }
                """

                /*
                 * Step 1:
                 * Store the failed incident/log.
                 *
                 * Change /logs/bulk if your actual ingestion
                 * endpoint uses a different request structure.
                 */
                sh """
                    curl -X POST '${LOG_ANALYZER_URL}/logs/bulk' \
                    -H 'Content-Type: application/json' \
                    -d '${payload}'
                """

                echo 'Requesting RAG analysis...'

                /*
                 * Step 2:
                 * Ask the AI DevOps Log Analyzer to search
                 * historical incidents and analyze this failure.
                 */
                def analysisPayload = """
                {
                    "query": "JOB: ${env.JOB_NAME}
                    BUILD: ${buildId}
                    FAILED STAGE: ${failedStage}
                    ERROR: ${escapedLogs}"
                }
                """

                sh """
                    curl -X POST '${LOG_ANALYZER_URL}/logs/analyze' \
                    -H 'Content-Type: application/json' \
                    -d '${analysisPayload}'
                """
            }
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}
