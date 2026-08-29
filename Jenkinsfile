import java.io.BufferedReader
import java.io.FileReader
import java.util.Base64

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
                        sendFullStageNotification('Checkout', 'SUCCESS', startTime)
                    } catch (Exception e) {
                        sendFullStageNotification('Checkout', 'FAILED', startTime)
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
                        sendFullStageNotification('Build', 'SUCCESS', startTime)
                    } catch (Exception e) {
                        sendFullStageNotification('Build', 'FAILED', startTime)
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
                        sendFullStageNotification('Deploy', 'SUCCESS', startTime)
                    } catch (Exception e) {
                        sendFullStageNotification('Deploy', 'FAILED', startTime)
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

// Function to extract stage logs, encode to Base64, and post cleanly using a file wrapper
// Safe function to pull exact stage logs from execution memory, encode to Base64, and post via file
def sendFullStageNotification(String stageName, String status, long startTime) {
    long endTime = System.currentTimeMillis()
    long duration = endTime - startTime
    
    String stageLogContent = ""
    
    try {
        // Read lines directly from the native Jenkins memory buffer safely (up to 10,000 lines)
        List<String> logLines = currentBuild.rawBuild.getLog(10000) [1]
        StringBuilder logBuilder = new StringBuilder()
        boolean insideTargetStage = false
        
        for (String line : logLines) {
            // Jenkins wraps stage boundaries in explicit markers
            if (line.contains("[Pipeline] { (" + stageName + ")")) {
                insideTargetStage = true
                continue
            }
            // Detect when this specific stage closes out
            if (insideTargetStage && line.contains("[Pipeline] }") && line.contains("Stage (" + stageName + ")")) {
                insideTargetStage = false
            }
            // Capture everything printed in between
            if (insideTargetStage) {
                logBuilder.append(line).append("\n")
            }
        }
        
        stageLogContent = logBuilder.toString()
        
        // If the specific block wasn't captured, grab the trailing log fragments as a backup
        if (stageLogContent.trim().isEmpty()) {
            int totalLines = logLines.size()
            int startIdx = Math.max(0, totalLines - 100) // Fallback to last 100 lines of execution context
            for (int i = startIdx; i < totalLines; i++) {
                logBuilder.append(logLines.get(i)).append("\n")
            }
            stageLogContent = "--- Fallback Log Window ---\n" + logBuilder.toString()
        }
    } catch (Exception ex) {
        stageLogContent = "Could not pull live Jenkins logs programmatically: ${ex.message}"
    }

    // Convert raw log string into a safe Base64 string directly in memory
    String base64Logs = ""
    try {
        base64Logs = java.util.Base64.getEncoder().encodeToString(stageLogContent.getBytes("UTF-8"))
    } catch (Exception e) {
        base64Logs = java.util.Base64.getEncoder().encodeToString("Error encoding logs to Base64".getBytes("UTF-8"))
    }

    // Create the final payload map with the clean Base64 string assigned to the message field
    String jsonPayload = """{
  "id": "${env.JOB_NAME}-${env.BUILD_NUMBER}-${stageName.replaceAll(' ', '_')}",
  "level": "${(status == 'FAILED') ? 'ERROR' : 'INFO'}",
  "message": "${base64Logs}",
  "timestamp": ${endTime},
  "metadata": {
    "jobName": "${env.JOB_NAME}",
    "buildNumber": "${env.BUILD_NUMBER}",
    "stageName": "${stageName}",
    "status": "${status}",
    "durationMs": ${duration}
  }
}"""

    String filename = "payload_${stageName.replaceAll(' ', '_')}.json"
    try {
        // Save the JSON payload to the workspace disk to bypass shell character limitations
        writeFile file: filename, text: jsonPayload, encoding: 'UTF-8'
        
        // Use '@' notation to instruct curl to push the binary file contents safely
        bat """
            curl --location "${env.LOCAL_API_URL}" ^
            --header "Content-Type: application/json" ^
            --data-binary "@${filename}"
        """
    } catch (Exception e) {
        echo "Failed to dispatch complete log data to endpoint for stage ${stageName}: ${e.message}"
    } finally {
        // Safe file cleanup step using native Windows commands
        bat "del /f /q ${filename} 2>nul || ver > nul"
    }
}
