pipeline {
    agent any

    environment {
        APP_NAME        = 'simple-rest-api'
        IMAGE_TAG       = "${BUILD_NUMBER}"
        CONTAINER_NAME  = 'simple-api-container'
        HOST_PORT       = '8082'
        CONTAINER_PORT  = '8080'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image (CI)') {
            steps {
                echo "Building Docker image: ${APP_NAME}:${IMAGE_TAG}..."
                bat "docker build -t ${APP_NAME}:${IMAGE_TAG} -t ${APP_NAME}:latest ."
            }
        }

        stage('Deploy Container (CD)') {
            steps {
                echo "Deploying container on port ${HOST_PORT}..."
                bat """
                    docker stop ${CONTAINER_NAME} || ver > nul
                    docker rm ${CONTAINER_NAME} || ver > nul
                    docker run -d --name ${CONTAINER_NAME} --restart unless-stopped -p ${HOST_PORT}:${CONTAINER_PORT} ${APP_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage('Health Check') {
            steps {
                echo "Waiting 15 seconds for Spring Boot container to initialize..."
                // Native Jenkins pipeline sleep (avoids Windows batch redirection errors)
                sleep(time: 15, unit: 'SECONDS')

                echo "Verifying API health endpoint..."
                bat """
                    curl --retry 5 --retry-connrefused --retry-delay 3 -f http://localhost:${HOST_PORT}/api/hello || exit 1
                """
            }
        }
    }

    post {
        success {
            echo "Container deployed! Swagger UI is accessible at: http://localhost:${HOST_PORT}/swagger-ui/index.html"
        }
        failure {
            echo "Build failed. Check docker logs or build console output."
        }
    }
}