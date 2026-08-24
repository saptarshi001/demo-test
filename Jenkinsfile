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
                echo "Testing REST API endpoints..."
                bat """
                    timeout /t 10 /nobreak
                    curl -f http://localhost:${HOST_PORT}/api/hello || exit 1
                """
            }
        }
    }

    post {
        success {
            echo "Deployment verified! Test your API at http://localhost:${HOST_PORT}/api/hello"
        }
        failure {
            echo "Pipeline failed. Check container logs using 'docker logs ${CONTAINER_NAME}'"
        }
    }
}