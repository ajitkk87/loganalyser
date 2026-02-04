package com.analyser.loganalyser.controller;

import com.analyser.loganalyser.service.LogAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class LogAnalysisController {

    private final LogAnalysisService logService;

    // Spring injects the service automatically
    public LogAnalysisController(LogAnalysisService logService) {
        this.logService = logService;
    }

    /**
     * Analyze raw log text sent in the request body
     */
    @PostMapping("/search-and-analyze-env")
    public ResponseEntity<String> searchAndAnalyzeEnv(
            @RequestParam(defaultValue = "TST") String env,
            @RequestParam(defaultValue = "Find critical errors") String query,
            @RequestParam(required = false) String repoLink,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String applicationName) {
        // Assuming your service has a method to search the vector store
        String analysis = logService.processLogs(env, query, repoLink, logLevel, days, applicationName);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Search and analyze logs from ELK/VectorStore based on a query
     */
    @PostMapping("/search-and-analyze-raw")
    public ResponseEntity<String> searchAndAnalyzeRaw(
            @RequestBody String logData,
            @RequestParam(defaultValue = "Find critical errors") String query,
            @RequestParam(required = false) String repoLink,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String applicationName) {
        // Assuming your service has a method to search the vector store
        String analysis = logService.processLogs(logData, query, repoLink, logLevel, days, applicationName);
        return ResponseEntity.ok(analysis);
    }
}
