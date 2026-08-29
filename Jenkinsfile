pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME        = 'simple-rest-api'
        IMAGE_TAG       = "${BUILD_NUMBER}"
        CONTAINER_NAME  = 'simple-api-container'
        HOST_PORT       = '8082'
        CONTAINER_PORT  = '8080'
        LOCAL_API_URL   = 'http://localhost:8080/api/logs'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    long startTime = System.currentTimeMillis()
                    try {
                        checkout scm
                        sendStatusNotification('Checkout', 'SUCCESS', startTime)
                    } catch (Exception e) {
                        sendStatusNotification('Checkout', 'FAILED', startTime)
                        throw e
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    long startTime = System.currentTimeMillis()
                    try {
                        echo "Building Docker image: ${APP_NAME}:${IMAGE_TAG}..."
                        bat "docker build -t ${APP_NAME}:${IMAGE_TAG} -t ${APP_NAME}:latest ."
                        sendStatusNotification('Build', 'SUCCESS', startTime)
                    } catch (Exception e) {
                        sendStatusNotification('Build', 'FAILED', startTime)
                        throw e
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    long startTime = System.currentTimeMillis()
                    try {
                        echo "Replacing old container and starting ${APP_NAME}:${IMAGE_TAG} on port ${HOST_PORT}..."
                        bat """
                            docker rm -f ${CONTAINER_NAME} 2>nul || ver > nul
                            docker run -d --name ${CONTAINER_NAME} --restart unless-stopped -p ${HOST_PORT}:${CONTAINER_PORT} ${APP_NAME}:${IMAGE_TAG}
                        """
                        
                        echo "Waiting for container initialization..."
                        sleep(time: 15, unit: 'SECONDS')

                        echo "Verifying API health endpoint..."
                        retry(3) {
                            bat """
                                curl --fail --retry 3 --retry-connrefused --retry-delay 2 http://localhost:${HOST_PORT}/api/hello
                            """
                        }
                        sendStatusNotification('Deploy', 'SUCCESS', startTime)
                    } catch (Exception e) {
                        sendStatusNotification('Deploy', 'FAILED', startTime)
                        throw e
                    }
                }
            }
        }
    }

    post {
        always {
            bat "docker image prune -f 2>nul || ver > nul"
        }
        failure {
            echo "Pipeline failed. Fetching container logs for debugging:"
            bat "docker logs --tail 100 ${CONTAINER_NAME} 2>nul || ver > nul"
        }
    }
}

// Reusable Shared Function using Windows bat curl to dispatch data
def sendStatusNotification(String stageName, String status, long startTime) {
    long endTime = System.currentTimeMillis()
    long duration = endTime - startTime
    
    // Format JSON safely for Windows batch execution (double quotes escaped as \")
    String payload = """{
        \\"id\\": \\"${env.JOB_NAME}-${env.BUILD_NUMBER}-${stageName.replaceAll(' ', '_')}\\",
        \\"level\\": \\"${(status == 'FAILED') ? 'ERROR' : 'INFO'}\\",
        \\"message\\": \\"Stage '${stageName}' finished with status: ${status}\\",
        \\"timestamp\\": ${endTime},
        \\"metadata\\": {
            \\"jobName\\": \\"${env.JOB_NAME}\\",
            \\"buildNumber\\": \\"${env.BUILD_NUMBER}\\",
            \\"stageName\\": \\"${stageName}\\",
            \\"status\\": \\"${status}\\",
            \\"durationMs\\": ${duration}
        }
    }"""

    try {
        // Run curl step natively matching your precise endpoint specifications
        bat """
            curl --location "${env.LOCAL_API_URL}" ^
            --header "Content-Type: application/json" ^
            --data "${payload.replaceAll('\n', '').replaceAll(' +', ' ')}"
        """
    } catch (Exception e) {
        // Keeps pipeline healthy if the logging infrastructure acts up
        echo "Failed to dispatch log via curl for stage ${stageName}: ${e.message}"
    }
}