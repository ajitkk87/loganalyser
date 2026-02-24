package com.analyser.loganalyser.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.analyser.loganalyser.model.LogAnalysisRequest;
import com.analyser.loganalyser.model.agent.AgentCard;
import com.analyser.loganalyser.model.agent.AgentInvokeRequest;
import com.analyser.loganalyser.model.agent.AgentInvokeResponse;
import com.analyser.loganalyser.service.LogAnalysisService;
import org.junit.jupiter.api.Test;

class AgentControllerTest {

    private final LogAnalysisService logAnalysisService =
            org.mockito.Mockito.mock(LogAnalysisService.class);
    private final AgentController agentController = new AgentController(logAnalysisService);

    @Test
    void getCard_shouldReturnAgentMetadata() {
        AgentCard card = agentController.getCard().getBody();
        assertThat(card).isNotNull();
        assertThat(card.id()).isEqualTo("log-analyser-agent");
        assertThat(card.name()).isEqualTo("Log Analyser Agent");
        assertThat(card.invokeEndpoint()).isEqualTo("/api/agent/analyze");
    }

    @Test
    void analyze_shouldDelegateToServiceAndReturnAnalysis() {
        LogAnalysisRequest logRequest =
                new LogAnalysisRequest(
                        "ERROR: timeout",
                        "Find root cause",
                        "https://example.com/repo",
                        "ERROR",
                        2,
                        "payments",
                        "test");
        when(logAnalysisService.processLogs(logRequest)).thenReturn("Analysis complete");

        AgentInvokeRequest request =
                new AgentInvokeRequest(
                        "ERROR: timeout",
                        "Find root cause",
                        "https://example.com/repo",
                        "ERROR",
                        2,
                        "payments",
                        "test");
        AgentInvokeResponse response = agentController.analyze(request).getBody();
        assertThat(response).isNotNull();
        assertThat(response.analysis()).isEqualTo("Analysis complete");

        verify(logAnalysisService).processLogs(logRequest);
    }
}
