package com.example.demo.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health & Greetings API", description = "Endpoints for pipeline health verification and greeting responses")
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    @Operation(summary = "Service Health Check", description = "Returns service operational status for CI/CD health probes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is up and running")
    })
    @GetMapping("/hello")
    public Map<String, String> hello() {
        log.info("[REST_ACCESS] GET /api/hello called - returning status UP");
        return Map.of("status", "UP", "message", "Application is running cleanly!");
    }

    @Operation(summary = "User Greeting", description = "Generates a personalized greeting message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Greeting successfully generated")
    })
    @GetMapping("/greet/{name}")
    public Map<String, String> greet(
            @Parameter(description = "Name of the person to greet", example = "Saptarshi")
            @PathVariable String name) {
        log.info("[REST_ACCESS] GET /api/greet/{} received", name);
        return Map.of("greeting", "Hello, " + name + "!");
    }
}