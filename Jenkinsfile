pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    tools {
        // Ensure Maven and JDK match the names configured under 'Global Tool Configuration'
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }

    environment {
        APP_NAME        = 'simple-rest-api'
        IMAGE_TAG       = "${BUILD_NUMBER}"
        CONTAINER_NAME  = 'simple-api-container'
        HOST_PORT       = '8082'
        CONTAINER_PORT  = '8080'
        NOTIFICATION_TO = 'team-devops@example.com'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test & Code Coverage') {
            steps {
                echo "Running unit tests and generating JaCoCo coverage report..."
                // Runs Maven build, runs JUnit tests, and creates jacoco coverage exec/xml
                bat "mvn clean test jacoco:report"
            }
            post {
                always {
                    // Record JUnit Test Results
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

                    // Publish Code Coverage Results
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                        id: 'jacoco',
                        name: 'JaCoCo Coverage'
                    )
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image: ${APP_NAME}:${IMAGE_TAG}..."
                bat "docker build -t ${APP_NAME}:${IMAGE_TAG} -t ${APP_NAME}:latest ."
            }
        }

        stage('Deploy Container') {
            steps {
                echo "Replacing old container and starting ${APP_NAME}:${IMAGE_TAG} on port ${HOST_PORT}..."
                bat """
                    docker rm -f ${CONTAINER_NAME} 2>nul || ver > nul
                    docker run -d --name ${CONTAINER_NAME} --restart unless-stopped -p ${HOST_PORT}:${CONTAINER_PORT} ${APP_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage('Health Check') {
            steps {
                echo "Waiting for container initialization..."
                sleep(time: 15, unit: 'SECONDS')

                echo "Verifying API health endpoint..."
                retry(3) {
                    bat """
                        curl --fail --retry 3 --retry-connrefused --retry-delay 2 http://localhost:${HOST_PORT}/api/hello
                    """
                }
            }
        }
    }

    post {
        always {
            // Prune dangling images created during the build step
            bat "docker image prune -f 2>nul || ver > nul"

            // Asynchronous, non-blocking email notification
            script {
                parallel(
                    "Async Email Notification": {
                        try {
                            emailext (
                                to: "${NOTIFICATION_TO}",
                                subject: "Build ${currentBuild.currentResult}: Job '${env.JOB_NAME}' [${env.BUILD_NUMBER}]",
                                body: """
                                    <p>Build Status: <b>${currentBuild.currentResult}</b></p>
                                    <p>Project: ${env.JOB_NAME} (Build #${env.BUILD_NUMBER})</p>
                                    <p>API Endpoint: <a href="http://localhost:${HOST_PORT}/api/hello">http://localhost:${HOST_PORT}/api/hello</a></p>
                                    <p>Swagger UI: <a href="http://localhost:${HOST_PORT}/swagger-ui/index.html">http://localhost:${HOST_PORT}/swagger-ui/index.html</a></p>
                                    <p>Check console output: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                                """,
                                mimeType: 'text/html'
                            )
                        } catch (Exception e) {
                            echo "Async email notification could not be dispatched: ${e.message}"
                        }
                    }
                )
            }
        }
        failure {
            echo "Pipeline failed. Fetching container logs for debugging:"
            bat "docker logs --tail 100 ${CONTAINER_NAME} 2>nul || ver > nul"
        }
    }
}