package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Demo Controller", description = "Endpoints for CI/CD health verification and greetings")
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    @Operation(
        summary = "Check API Deployment Status",
        description = "Returns operational status, deployment message, and timestamp for health probes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is UP and running cleanly")
    })
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> getHello() {
        log.info("[REST_ACCESS] GET /api/hello called - health probe check");

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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Greeting generated successfully")
    })
    @GetMapping("/greet/{name}")
    public ResponseEntity<Map<String, String>> greetUser(
            @Parameter(description = "Name of the person to greet", example = "Saptarshi")
            @PathVariable String name) {

        log.info("[REST_ACCESS] GET /api/greet/{} received", name);

        return ResponseEntity.ok(Map.of(
            "greeting", "Hello, " + name + "!",
            "deployedVia", "Jenkins Docker Pipeline",
            "timestamp", Instant.now().toString()
        ));
    }
}