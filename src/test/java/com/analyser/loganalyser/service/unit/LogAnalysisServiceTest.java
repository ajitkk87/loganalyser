package com.analyser.loganalyser.service.unit;

import com.analyser.loganalyser.config.LogProperties;
import com.analyser.loganalyser.service.LogAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAnalysisServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private LogProperties logProperties;

    @InjectMocks
    private LogAnalysisService logAnalysisService;

    @Test
    void processLogs_shouldCallChatModelAndReturnResponse() {
        // Given
        String rawLogs = "ERROR: Connection timeout";
        String expectedResponse = "The logs indicate a connection timeout error.";
        when(chatModel.call(anyString())).thenReturn(expectedResponse);

        // When
        String result = logAnalysisService.processLogs(rawLogs);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatModel).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("Identify errors in these logs");
        assertThat(promptCaptor.getValue()).contains(rawLogs);
        assertThat(promptCaptor.getValue()).contains("Provide the output in a consistent tabular format");
    }

    @Test
    void processLogs_withRepoLink_shouldIncludeRepoLinkInPrompt() {
        // Given
        String rawLogs = "ERROR: Connection timeout";
        String repoLink = "https://github.com/example/repo";
        String expectedResponse = "Analysis with repo context.";
        when(chatModel.call(anyString())).thenReturn(expectedResponse);

        // When
        String result = logAnalysisService.processLogs(rawLogs, null, repoLink, null, null, null);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatModel).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("Context repository: https://github.com/example/repo");
    }

    @Test
    void processLogs_withEnv_shouldFetchLogsFromEnv() {
        // Given
        String env = "PROD";
        String expectedResponse = "Analysis of PROD logs.";
        when(logProperties.getEnvUrls()).thenReturn(Map.of("PROD", "http://prod.example.com"));
        when(chatModel.call(anyString())).thenReturn(expectedResponse);

        // When
        String result = logAnalysisService.processLogs(null, null, null, null, null, null, env);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatModel).call(promptCaptor.capture());
        // Verify that the mock logs from fetchLogsFromEnv are included
        assertThat(promptCaptor.getValue()).contains("Fetching logs from 'PROD' environment");
    }
    
    @Test
    void processLogs_withEnvAndFilters_shouldIncludeFiltersInMockLogs() {
        // Given
        String env = "PROD";
        String logLevel = "ERROR";
        String appName = "UserService";
        int days = 3;
        String expectedResponse = "Analysis of filtered PROD logs.";
        when(logProperties.getEnvUrls()).thenReturn(Map.of("PROD", "http://prod.example.com"));
        when(chatModel.call(anyString())).thenReturn(expectedResponse);

        // When
        String result = logAnalysisService.processLogs(null, null, null, logLevel, days, appName, env);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatModel).call(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        
        // Verify filters are passed to the mock log fetcher
        assertThat(prompt).contains("Fetching logs from 'PROD' environment");
        assertThat(prompt).contains("for the last 3 days");
        assertThat(prompt).contains("for application 'UserService'");
        assertThat(prompt).contains("with level 'ERROR'");
    }

    @Test
    void processLogs_shouldThrowException_whenLogLengthExceedsLimit() {
        // Given
        StringBuilder hugeLog = new StringBuilder();
        for (int i = 0; i < 1_000_001; i++) {
            hugeLog.append("a");
        }
        String rawLogs = hugeLog.toString();

        // When & Then
        assertThatThrownBy(() -> logAnalysisService.processLogs(rawLogs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Raw logs length exceeds the limit");
    }

    @Test
    void processLogs_shouldThrowException_whenQueryLengthExceedsLimit() {
        // Given
        String rawLogs = "logs";
        StringBuilder hugeQuery = new StringBuilder();
        for (int i = 0; i < 100_001; i++) {
            hugeQuery.append("a");
        }
        String query = hugeQuery.toString();

        // When & Then
        assertThatThrownBy(() -> logAnalysisService.processLogs(rawLogs, query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Query length exceeds the limit");
    }
}