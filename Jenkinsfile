pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    environment {
        APP_NAME       = 'simple-rest-api'
        IMAGE_TAG      = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'simple-api-container'
        HOST_PORT      = '8082'
        CONTAINER_PORT = '8080'
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
                echo "Building application..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                sh 'mvn test'
            }
        }

        stage('Deploy') {
            steps {
                echo "Building Docker image..."
                sh """
                    docker build \
                        -t ${APP_NAME}:${IMAGE_TAG} \
                        -t ${APP_NAME}:latest .
                """

                echo "Stopping old container if it exists..."
                sh """
                    docker rm -f ${CONTAINER_NAME} || true
                """

                echo "Starting new container..."
                sh """
                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        --restart unless-stopped \
                        -p ${HOST_PORT}:${CONTAINER_PORT} \
                        ${APP_NAME}:${IMAGE_TAG}
                """

                echo "Deployment completed successfully."
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed.'
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}