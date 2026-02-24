package com.analyser.loganalyser.controller;

import com.analyser.loganalyser.model.LogAnalysisRequest;
import com.analyser.loganalyser.model.agent.AgentCard;
import com.analyser.loganalyser.model.agent.AgentInvokeRequest;
import com.analyser.loganalyser.model.agent.AgentInvokeResponse;
import com.analyser.loganalyser.service.LogAnalysisService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final LogAnalysisService logAnalysisService;

    public AgentController(LogAnalysisService logAnalysisService) {
        this.logAnalysisService = logAnalysisService;
    }

    @GetMapping("/card")
    public ResponseEntity<AgentCard> getCard() {
        return ResponseEntity.ok(buildCard());
    }

    @GetMapping("/.well-known/agent-card")
    public ResponseEntity<AgentCard> getWellKnownCard() {
        return ResponseEntity.ok(buildCard());
    }

    @PostMapping("/analyze")
    public ResponseEntity<AgentInvokeResponse> analyze(@RequestBody AgentInvokeRequest request) {
        String analysis =
                logAnalysisService.processLogs(
                        new LogAnalysisRequest(
                                request.logs(),
                                request.query(),
                                request.repoLink(),
                                request.logLevel(),
                                request.days(),
                                request.applicationName(),
                                request.environment()));
        return ResponseEntity.ok(new AgentInvokeResponse(analysis));
    }

    private AgentCard buildCard() {
        return new AgentCard(
                "log-analyser-agent",
                "Log Analyser Agent",
                "AI-powered root-cause analysis for raw and environment-sourced logs.",
                "1.0.0",
                List.of("log-analysis", "error-detection", "remediation-suggestions"),
                "/api/agent/analyze");
    }
}
