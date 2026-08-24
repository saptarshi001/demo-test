package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // Health / Root endpoint
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> getHello() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "CI/CD Pipeline Deployment Successful!",
            "timestamp", Instant.now().toString()
        ));
    }

    // Dynamic Parameterized Endpoint
    @GetMapping("/greet/{name}")
    public ResponseEntity<Map<String, String>> greetUser(@PathVariable String name) {
        return ResponseEntity.ok(Map.of(
            "greeting", "Hello, " + name + "!",
            "deployedVia", "Jenkins Docker Pipeline"
        ));
    }
}