package com.project.cicd_auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

// Server Toggle 역할 수행
@RestController
public class HealthCheckController {

    @Value("${server.name}")
    private String serverName;
    @Value("${server.host}")
    private String serverHost;
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.env}")
    private String env;

    @GetMapping("/hc")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> responseData = new LinkedHashMap<>();
        responseData.put("serverName", serverName);
        responseData.put("serverHost", serverHost);
        responseData.put("serverPort", serverPort);
        responseData.put("env", env);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/env")
    public ResponseEntity<?> getEnv() {
        return ResponseEntity.ok(env);
    }
}
