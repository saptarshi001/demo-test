package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Demo REST API",
        version = "1.0",
        description = "Documentation for CI/CD Pipeline Demo Service"
    )
)
@RestController
@RequestMapping("/api")
@Tag(name = "Demo Controller", description = "Endpoints for CI/CD and Health verification")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Operation(
        summary = "Check API Deployment Status",
        description = "Returns current status, deployment message, and timestamp"
    )
    @ApiResponse(responseCode = "200", description = "Application is UP and running")
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> getHello() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "CI/CD Pipeline Deployment Successful with Swagger UI!",
            "timestamp", Instant.now().toString()
        ));
    }

    @Operation(
        summary = "Greet User",
        description = "Returns a personalized greeting along with deployment metadata"
    )
    @ApiResponse(responseCode = "200", description = "Greeting generated successfully")
    @GetMapping("/greet/{name}")
    public ResponseEntity<Map<String, String>> greetUser(@PathVariable String name) {
        return ResponseEntity.ok(Map.of(
            "greeting", "Hello, " + name + "!",
            "deployedVia", "Jenkins Docker Pipeline"
        ));
    }
}