package com.omaap.loganalyser.controller;

import com.omaap.loganalyser.service.LogAnalysisService;
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
    @PostMapping("/analyze-raw")
    public ResponseEntity<String> analyzeRaw(@RequestBody String logData) {
        String analysis = logService.processLogs(logData);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Search and analyze logs from ELK/VectorStore based on a query
     */
    @GetMapping("/search-and-analyze")
    public ResponseEntity<String> searchAndAnalyze(@RequestParam(defaultValue = "Find critical errors") String query) {
        // Assuming your service has a method to search the vector store
        String analysis = logService.processLogs(query);
        return ResponseEntity.ok(analysis);
    }
}
